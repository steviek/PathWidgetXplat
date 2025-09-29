package com.desaiwang.transit.path.test

internal val Alerts = """
    {
      "alerts": [
        {
          "stations": [
            "09S",
            "23S"
          ],
          "schedule": {
            "repeatingDaily": {
              "days": [
                "MONDAY",
                "TUESDAY",
                "WEDNESDAY",
                "THURSDAY",
                "FRIDAY",
                "SATURDAY",
                "SUNDAY"
              ],
              "start": "00:00",
              "end": "05:00",
              "from": "2024-05-29",
              "to": "2025-12-31"
            }
          },
          "trains": {
            "all": true
          },
          "message": {
            "localizations": [
              {
                "text": "9 St. & 23 St. stations are closed daily from 11:59 PM – 5 AM for maintenance-related activity. Please use Christopher St., 14 St., or 33 St. station.",
                "locale": "en"
              },
              {
                "text": "Las estaciones de 9 St. y 23 St. están cerradas diariamente de 11:59 p. m. a 5 a. m. para actividades relacionadas con el mantenimiento. Utilice las estaciones de Christopher St., 14 St. o 33 St.",
                "locale": "es"
              }
            ]
          },
          "url": {
            "localizations": [
              {
                "text": "https://www.panynj.gov/path/en/schedules-maps.html",
                "locale": "en"
              }
            ]
          },
          "displaySchedule": {
            "repeatingDaily": {
              "days": [
                "MONDAY",
                "TUESDAY",
                "WEDNESDAY",
                "THURSDAY",
                "FRIDAY",
                "SATURDAY",
                "SUNDAY"
              ],
              "start": "17:00",
              "end": "05:00",
              "from": "2024-04-06",
              "to": "2024-06-30"
            }
          },
          "level": "WARN"
        },
        {
          "stations": [
            "14S"
          ],
          "schedule": {},
          "trains": {},
          "message": {
            "localizations": [
              {
                "text": "During overnight hours all PATH service at the 14 Street Station may operate from the station’s uptown or downtown track.  Signage is posted at the entrance of the station when this single-track operation is in effect.",
                "locale": "en"
              }
            ]
          },
          "url": {
            "localizations": [
              {
                "text": "https://www.panynj.gov/path/en/planned-service-changes.html",
                "locale": "en"
              }
            ]
          },
          "displaySchedule": {
            "repeatingDaily": {
              "days": [
                "MONDAY",
                "TUESDAY",
                "WEDNESDAY",
                "THURSDAY",
                "FRIDAY",
                "SATURDAY",
                "SUNDAY"
              ],
              "start": "22:00",
              "end": "07:00",
              "from": "2024-01-01",
              "to": "2025-12-31"
            }
          }
        }
      ]
    }
""".trimIndent()