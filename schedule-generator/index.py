import requests
import json
from create_override import create_override
from parser import parse_schedule_times
from datetime import datetime

def run():
    # fetch the entire website structure... what CMS are they even using?????????
    URL = "https://www.panynj.gov/content/path/en.model.json"
    resp = requests.get(URL)
    # parse the json
    page = resp.json()
    # do regulars
    write_json(create_regular(page), "schedule.json")
    # do overrides
    write_json(create_override(page), "schedule_override.json")

def create_regular(page: dict) -> dict:
    r = dict()
    r['schedules'] = list()
    r['timings'] = list()
    r['name'] = "regular"
    dates = list()
    titles = (
        ("/path/en/schedules-maps/weekday-schedules", 1, 6, 1),
        ("/path/en/schedules-maps/saturday-schedules", 6, 7, 2),
        ("/path/en/schedules-maps/sunday-schedules", 7, 1, 3)
    )
    for link, startDay, endDay, scheduleId in titles:
        # process timing
        t = dict()
        t['scheduleId'] = scheduleId
        t['startDay'] = startDay
        t['startTime'] = 0
        t['endDay'] = endDay
        t['endTime'] = 0
        r['timings'].append(t)
        # process schedule
        data = page[":children"][link][":items"]["root"][":items"]
        dates.append(datetime.strptime(
            data["textblock"]["text"].replace("&nbsp;", " ").split("Effective ")[1].split("</p>")[0],
            "%B %d, %Y"
        ))
        title = data["simplehero_copy"]["title"]
        r['schedules'].append(parse_schedule_times(
            data["accordionlist"][":items"].items(),
            scheduleId,
            title
        ))
    dates.sort(reverse=True)
    r['validFrom'] = dates[0].strftime("%Y-%m-%dT00:00")
    return r

def write_json(d: dict, file: str):
    if d == dict():
        return
    with open(file, 'r+') as f:
        old_data = json.load(f)
        # for use by CI, we don't want to run it again if there's no change from the last run
        if d != old_data:
            f.truncate(0)
            f.seek(0)
            json.dump(d, f, separators=(',', ':'))
            return d

if __name__ == "__main__":
    run()
