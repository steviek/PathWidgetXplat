from typing import Any
from bs4 import BeautifulSoup
import re

def parse_schedule_times(services: list, scheduleId: int, title: str) -> dict[str, Any]:
    # process schedule
    s = dict()
    s['id'] = scheduleId
    s['name'] = title
    s['departures'] = dict()
    # process each service's schedule
    for subkey, service in services:
        if not subkey.startswith("accordion"):
            continue
        # process service name
        service_code = get_code(service["accordionLabel"])
        if service_code == "":
            continue
        # read each timetable
        items = service[":items"]
        first_key = next(iter(items))
        for _, textblock in items.items():
            soup = BeautifulSoup(textblock["text"], "html.parser")
            departures = list()
            first_row_read = False
            # process timetable
            for train in soup.select("table tr td:nth-of-type(1)"):
                if first_row_read:
                    parsed = parse_time(train.string)
                    if parsed is not None:
                        departures.append(parse_time(train.string))
                else:
                    first_row_read = True
            if len(departures) > 0:
                s['departures'][service_code] = departures
                break
    return s

def get_code(service: str) -> str:
    service = service.replace("(", "").replace(")", "")
    m = re.search("(?:[\\w\\d]+ )+- (?:[\\w\\d]+ )*[\\w\\d]+", service)
    match m.group(0):
        case "Newark - World Trade Center":
            return "NWK_WTC"
        case "World Trade Center - Newark":
            return "WTC_NWK"
        case "Journal Square - 33 Street":
            return "JSQ_33S"
        case "33 Street - Journal Square":
            return "33S_JSQ"
        case "Journal Square - 33 Street via Hoboken":
            return "JSQ_HOB_33S"
        case "33 Street - Journal Square via Hoboken":
            return "33S_HOB_JSQ"
        case "World Trade Center - Hoboken":
            return "WTC_HOB"
        case "Hoboken - World Trade Center":
            return "HOB_WTC"
        case "33 Street - Hoboken":
            return "33S_HOB"
        case "Hoboken - 33 Street":
            return "HOB_33S"
    return ""

def parse_time(time: str | None) -> int | None:
    if time is None:
        return None
    m = re.search("^(\\d{1,2}):(\\d{2}) (AM|PM)$", time)
    if m is None:
        return None
    hour = 0 if m.group(1) == "12" else int(m.group(1))
    hour = hour if m.group(3) == "AM" else hour + 12
    return hour * 100 + int(m.group(2))

def find_element_key(o: list[str], prefix: str) -> str | None:
    return next((x for x in o if x.startswith(prefix)), None)