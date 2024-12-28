from datetime import datetime, timedelta
from parser import parse_schedule_times

schedule_id_offset = 10

def create_override(page: dict) -> dict:
    date = None
    data = dict()
    # get every link
    for _, item in page[":children"]["/path/en/planned-service-changes"][":items"]["root"][":items"].items():
        if 'linkTitle' not in item or item['linkTitle'] != "See schedules":
            continue
        link = item['linkHref'].split(".html")[0]
        s, d = parse_schedule(page[":children"][link][":items"]["root"][":items"])
        # get only the closest date
        if date == None:
            data = s
            date = d
        elif d < date:
            data = s
            date = d
    return data

def parse_schedule(d: dict) -> tuple:
    r = dict()
    r['schedules'] = list()
    r['timings'] = list()
    # parse the title
    r['name'] = d["imagewithtext_copy_c"]["title"]
    # get every schedule block
    index = 0
    dates = []
    for key, block in d.items():
        if not key.startswith("accordionlist_copy_c"):
            continue
        # process date
        date = datetime.strptime(block["title"], "%A, %B %d, %Y")
        dates.append(date)
        # process timing
        t = dict()
        t['scheduleId'] = schedule_id_offset + index
        t['startDay'] = date.isoweekday()
        t['startTime'] = 0
        t['endDay'] = 1 if date.isoweekday() == 7 else (date.isoweekday() + 1)
        t['endTime'] = 0
        r['timings'].append(t)
        r['schedules'].append(parse_schedule_times(block[":items"].items(), t['scheduleId'], block["title"]))
        index += 1
    r['validFrom'] = dates[0].strftime("%Y-%m-%dT00:00")
    r['validTo'] = (dates[-1] + timedelta(days=1)).strftime("%Y-%m-%dT00:00")
    return r, dates[0]
