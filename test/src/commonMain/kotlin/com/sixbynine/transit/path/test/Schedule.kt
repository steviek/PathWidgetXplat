package com.sixbynine.transit.path.test

internal val Schedule = """
    {
      "validFrom": "2024-04-07T00:00",
      "schedules": [
        {
          "id": 1,
          "name": "Regular Weekday Schedule",
          "departures": {
            "NWK_WTC": [
              30,
              110,
              150,
              230,
              310,
              350,
              430,
              500,
              515,
              530,
              545,
              556,
              601,
              606,
              611,
              616,
              621,
              626,
              631,
              636,
              641,
              646,
              651,
              656,
              701,
              706,
              711,
              716,
              721,
              726,
              731,
              736,
              741,
              746,
              751,
              756,
              801,
              806,
              811,
              816,
              821,
              826,
              831,
              836,
              841,
              846,
              851,
              856,
              901,
              906,
              911,
              916,
              921,
              926,
              931,
              936,
              941,
              946,
              1000,
              1020,
              1040,
              1100,
              1120,
              1140,
              1200,
              1220,
              1240,
              1300,
              1320,
              1340,
              1400,
              1420,
              1440,
              1500,
              1515,
              1530,
              1545,
              1556,
              1606,
              1611,
              1616,
              1621,
              1626,
              1631,
              1636,
              1641,
              1646,
              1651,
              1656,
              1701,
              1706,
              1711,
              1716,
              1721,
              1726,
              1731,
              1736,
              1741,
              1746,
              1751,
              1756,
              1801,
              1806,
              1811,
              1816,
              1821,
              1826,
              1831,
              1836,
              1841,
              1846,
              1851,
              1856,
              1901,
              1906,
              1911,
              1916,
              1926,
              1936,
              1946,
              1956,
              2006,
              2016,
              2026,
              2036,
              2046,
              2056,
              2106,
              2116,
              2130,
              2145,
              2200,
              2215,
              2230,
              2245,
              2320,
              2355
            ],
            "WTC_NWK": [
              30,
              110,
              150,
              230,
              310,
              350,
              430,
              505,
              530,
              545,
              600,
              614,
              624,
              629,
              634,
              639,
              644,
              649,
              654,
              659,
              704,
              709,
              714,
              719,
              724,
              729,
              734,
              739,
              744,
              749,
              754,
              759,
              804,
              809,
              814,
              819,
              824,
              829,
              834,
              839,
              844,
              849,
              854,
              859,
              904,
              909,
              914,
              919,
              924,
              929,
              934,
              939,
              949,
              1000,
              1020,
              1040,
              1100,
              1120,
              1140,
              1200,
              1220,
              1240,
              1300,
              1320,
              1340,
              1400,
              1420,
              1440,
              1500,
              1515,
              1530,
              1545,
              1554,
              1559,
              1604,
              1609,
              1614,
              1619,
              1624,
              1629,
              1634,
              1639,
              1644,
              1649,
              1654,
              1659,
              1704,
              1709,
              1714,
              1719,
              1724,
              1729,
              1734,
              1739,
              1744,
              1749,
              1754,
              1759,
              1804,
              1809,
              1814,
              1819,
              1824,
              1829,
              1834,
              1839,
              1844,
              1849,
              1854,
              1859,
              1904,
              1909,
              1914,
              1919,
              1924,
              1929,
              1934,
              1939,
              1944,
              1954,
              2004,
              2014,
              2024,
              2034,
              2044,
              2054,
              2104,
              2114,
              2124,
              2134,
              2145,
              2200,
              2215,
              2230,
              2245,
              2300,
              2320,
              2355
            ],
            "JSQ_33S": [
              549,
              559,
              609,
              619,
              629,
              634,
              639,
              644,
              649,
              654,
              659,
              704,
              709,
              714,
              719,
              724,
              729,
              734,
              739,
              744,
              749,
              754,
              759,
              804,
              809,
              814,
              819,
              824,
              829,
              834,
              839,
              844,
              849,
              854,
              859,
              904,
              909,
              914,
              919,
              924,
              929,
              934,
              939,
              948,
              1000,
              1012,
              1024,
              1036,
              1048,
              1100,
              1112,
              1124,
              1136,
              1148,
              1200,
              1212,
              1224,
              1236,
              1248,
              1300,
              1312,
              1324,
              1336,
              1348,
              1400,
              1412,
              1424,
              1436,
              1448,
              1459,
              1509,
              1519,
              1529,
              1539,
              1549,
              1554,
              1559,
              1604,
              1609,
              1614,
              1619,
              1624,
              1629,
              1634,
              1639,
              1644,
              1649,
              1654,
              1659,
              1704,
              1709,
              1714,
              1719,
              1724,
              1729,
              1734,
              1739,
              1744,
              1749,
              1754,
              1759,
              1804,
              1809,
              1814,
              1819,
              1824,
              1829,
              1834,
              1839,
              1844,
              1849,
              1859,
              1909,
              1919,
              1929,
              1939,
              1949,
              2000,
              2012,
              2024,
              2036,
              2048,
              2100,
              2112,
              2124,
              2136,
              2148,
              2200,
              2215,
              2230
            ],
            "33S_JSQ": [
              618,
              628,
              638,
              648,
              658,
              703,
              708,
              713,
              718,
              723,
              728,
              733,
              738,
              743,
              748,
              753,
              758,
              803,
              808,
              813,
              818,
              823,
              828,
              833,
              838,
              843,
              848,
              853,
              858,
              903,
              908,
              913,
              918,
              923,
              928,
              933,
              938,
              943,
              948,
              953,
              958,
              1008,
              1018,
              1030,
              1042,
              1054,
              1106,
              1118,
              1130,
              1142,
              1154,
              1206,
              1218,
              1230,
              1242,
              1254,
              1306,
              1318,
              1330,
              1342,
              1354,
              1406,
              1418,
              1430,
              1442,
              1454,
              1506,
              1518,
              1528,
              1538,
              1548,
              1558,
              1608,
              1618,
              1623,
              1628,
              1633,
              1638,
              1643,
              1648,
              1653,
              1658,
              1703,
              1708,
              1713,
              1718,
              1723,
              1728,
              1733,
              1738,
              1743,
              1748,
              1753,
              1758,
              1803,
              1808,
              1813,
              1818,
              1823,
              1828,
              1833,
              1838,
              1843,
              1848,
              1853,
              1858,
              1903,
              1908,
              1913,
              1918,
              1928,
              1938,
              1948,
              1958,
              2008,
              2018,
              2030,
              2042,
              2054,
              2106,
              2118,
              2130,
              2142,
              2154,
              2206,
              2218,
              2230,
              2245
            ],
            "JSQ_HOB_33S": [
              10,
              50,
              130,
              210,
              250,
              330,
              410,
              450,
              525,
              539,
              2245,
              2300,
              2315,
              2335
            ],
            "33S_HOB_JSQ": [
              13,
              48,
              128,
              208,
              248,
              328,
              408,
              448,
              528,
              548,
              608,
              2303,
              2323,
              2338,
              2353
            ],
            "WTC_HOB": [
              558,
              608,
              618,
              628,
              638,
              648,
              658,
              708,
              718,
              728,
              738,
              747,
              755,
              803,
              811,
              819,
              827,
              835,
              843,
              851,
              859,
              907,
              915,
              923,
              932,
              942,
              957,
              1012,
              1027,
              1042,
              1057,
              1112,
              1127,
              1142,
              1157,
              1212,
              1227,
              1242,
              1257,
              1312,
              1327,
              1342,
              1357,
              1412,
              1427,
              1442,
              1457,
              1512,
              1524,
              1536,
              1548,
              1600,
              1610,
              1620,
              1628,
              1636,
              1644,
              1652,
              1700,
              1708,
              1716,
              1724,
              1732,
              1740,
              1748,
              1756,
              1804,
              1812,
              1820,
              1828,
              1837,
              1847,
              1857,
              1907,
              1919,
              1931,
              1943,
              1955,
              2007,
              2019,
              2031,
              2043,
              2055,
              2107,
              2119,
              2131,
              2146,
              2201,
              2216,
              2236,
              2256
            ],
            "HOB_WTC": [
              615,
              625,
              635,
              645,
              655,
              705,
              715,
              725,
              734,
              742,
              750,
              758,
              806,
              814,
              822,
              830,
              838,
              846,
              854,
              902,
              910,
              918,
              926,
              934,
              943,
              953,
              1004,
              1019,
              1034,
              1049,
              1104,
              1119,
              1134,
              1149,
              1204,
              1219,
              1234,
              1249,
              1304,
              1319,
              1334,
              1349,
              1404,
              1419,
              1434,
              1449,
              1504,
              1518,
              1530,
              1542,
              1554,
              1606,
              1616,
              1626,
              1636,
              1646,
              1655,
              1703,
              1711,
              1719,
              1727,
              1735,
              1743,
              1751,
              1759,
              1807,
              1815,
              1823,
              1831,
              1839,
              1849,
              1901,
              1913,
              1925,
              1937,
              1949,
              2001,
              2013,
              2025,
              2037,
              2049,
              2101,
              2113,
              2128,
              2143,
              2158,
              2213,
              2228,
              2243,
              2258,
              2313
            ],
            "HOB_33S": [
              610,
              620,
              630,
              640,
              650,
              700,
              710,
              720,
              729,
              737,
              745,
              753,
              801,
              809,
              817,
              825,
              833,
              841,
              849,
              857,
              905,
              913,
              922,
              932,
              942,
              952,
              1002,
              1012,
              1022,
              1032,
              1043,
              1058,
              1113,
              1128,
              1143,
              1158,
              1213,
              1228,
              1243,
              1258,
              1313,
              1328,
              1343,
              1358,
              1413,
              1428,
              1442,
              1452,
              1502,
              1512,
              1522,
              1532,
              1542,
              1552,
              1602,
              1612,
              1622,
              1632,
              1642,
              1652,
              1700,
              1708,
              1716,
              1724,
              1732,
              1740,
              1748,
              1756,
              1804,
              1812,
              1820,
              1828,
              1836,
              1844,
              1852,
              1902,
              1912,
              1922,
              1932,
              1942,
              1952,
              2002,
              2012,
              2022,
              2032,
              2042,
              2057,
              2112,
              2127,
              2142,
              2157,
              2212,
              2227,
              2245
            ],
            "33S_HOB": [
              615,
              630,
              640,
              650,
              700,
              710,
              720,
              730,
              740,
              749,
              757,
              805,
              813,
              821,
              829,
              837,
              845,
              853,
              901,
              909,
              917,
              925,
              933,
              942,
              952,
              1002,
              1012,
              1022,
              1032,
              1042,
              1052,
              1105,
              1120,
              1135,
              1150,
              1205,
              1220,
              1235,
              1250,
              1305,
              1320,
              1335,
              1350,
              1405,
              1420,
              1435,
              1450,
              1502,
              1512,
              1522,
              1532,
              1542,
              1552,
              1602,
              1612,
              1622,
              1632,
              1640,
              1648,
              1656,
              1704,
              1712,
              1720,
              1728,
              1736,
              1744,
              1752,
              1800,
              1808,
              1816,
              1824,
              1832,
              1840,
              1848,
              1856,
              1904,
              1912,
              1922,
              1932,
              1942,
              1952,
              2002,
              2012,
              2022,
              2032,
              2042,
              2052,
              2102,
              2117,
              2132,
              2147,
              2202,
              2217,
              2232,
              2247
            ]
          }
        },
        {
          "id": 2,
          "name": "Regular Saturday Schedule",
          "departures": {
            "NWK_WTC": [
              30,
              110,
              150,
              230,
              310,
              350,
              430,
              510,
              545,
              620,
              655,
              720,
              740,
              800,
              820,
              840,
              900,
              920,
              940,
              1000,
              1020,
              1040,
              1100,
              1120,
              1140,
              1200,
              1220,
              1240,
              1300,
              1320,
              1340,
              1400,
              1420,
              1440,
              1500,
              1520,
              1540,
              1600,
              1620,
              1640,
              1700,
              1720,
              1740,
              1800,
              1820,
              1840,
              1900,
              1920,
              1940,
              2000,
              2020,
              2040,
              2100,
              2120,
              2140,
              2200,
              2220,
              2240,
              2300,
              2330,
              2355
            ],
            "WTC_NWK": [
              30,
              110,
              150,
              230,
              310,
              350,
              430,
              510,
              545,
              620,
              655,
              725,
              755,
              815,
              835,
              855,
              915,
              935,
              955,
              1015,
              1035,
              1055,
              1115,
              1135,
              1155,
              1215,
              1235,
              1255,
              1315,
              1335,
              1355,
              1415,
              1435,
              1455,
              1515,
              1535,
              1555,
              1615,
              1635,
              1655,
              1715,
              1735,
              1755,
              1815,
              1835,
              1855,
              1915,
              1935,
              1955,
              2015,
              2035,
              2055,
              2115,
              2135,
              2155,
              2215,
              2235,
              2255,
              2315,
              2335
            ],
            "JSQ_HOB_33S": [
              10,
              50,
              130,
              210,
              250,
              330,
              410,
              450,
              530,
              605,
              640,
              715,
              745,
              800,
              815,
              830,
              845,
              900,
              915,
              930,
              945,
              1000,
              1012,
              1024,
              1036,
              1048,
              1100,
              1112,
              1124,
              1136,
              1148,
              1200,
              1212,
              1224,
              1236,
              1248,
              1300,
              1312,
              1324,
              1336,
              1348,
              1400,
              1412,
              1424,
              1436,
              1448,
              1500,
              1510,
              1520,
              1530,
              1540,
              1550,
              1600,
              1610,
              1620,
              1630,
              1640,
              1650,
              1700,
              1710,
              1720,
              1730,
              1740,
              1750,
              1800,
              1810,
              1820,
              1830,
              1840,
              1850,
              1900,
              1910,
              1920,
              1930,
              1940,
              1950,
              2000,
              2010,
              2020,
              2030,
              2040,
              2050,
              2100,
              2112,
              2124,
              2136,
              2148,
              2200,
              2215,
              2230,
              2245,
              2300,
              2315,
              2330,
              2350
            ],
            "33S_HOB_JSQ": [
              13,
              48,
              128,
              208,
              248,
              328,
              408,
              448,
              528,
              608,
              643,
              718,
              753,
              823,
              838,
              853,
              908,
              923,
              938,
              953,
              1008,
              1023,
              1038,
              1050,
              1102,
              1114,
              1126,
              1138,
              1150,
              1202,
              1214,
              1226,
              1238,
              1250,
              1302,
              1314,
              1326,
              1338,
              1350,
              1402,
              1414,
              1426,
              1438,
              1450,
              1502,
              1514,
              1526,
              1538,
              1548,
              1558,
              1608,
              1618,
              1628,
              1638,
              1648,
              1658,
              1708,
              1718,
              1728,
              1738,
              1748,
              1758,
              1808,
              1818,
              1828,
              1838,
              1848,
              1858,
              1908,
              1918,
              1928,
              1938,
              1948,
              1958,
              2008,
              2018,
              2028,
              2038,
              2048,
              2058,
              2108,
              2118,
              2128,
              2138,
              2150,
              2202,
              2214,
              2226,
              2238,
              2253,
              2308,
              2323,
              2338,
              2353
            ]
          }
        },
        {
          "id": 3,
          "name": "Regular Sunday Schedule",
          "departures": {
            "NWK_WTC": [
              30,
              110,
              150,
              230,
              310,
              350,
              430,
              510,
              545,
              620,
              655,
              730,
              805,
              840,
              915,
              950,
              1020,
              1040,
              1100,
              1120,
              1140,
              1200,
              1220,
              1240,
              1300,
              1320,
              1340,
              1400,
              1420,
              1440,
              1500,
              1520,
              1540,
              1600,
              1620,
              1640,
              1700,
              1720,
              1740,
              1800,
              1820,
              1840,
              1900,
              1920,
              1940,
              2000,
              2020,
              2040,
              2100,
              2120,
              2140,
              2210,
              2245,
              2320,
              2355
            ],
            "WTC_NWK": [
              0,
              30,
              110,
              150,
              230,
              310,
              350,
              430,
              510,
              545,
              620,
              655,
              730,
              805,
              840,
              915,
              950,
              1025,
              1055,
              1115,
              1135,
              1155,
              1215,
              1235,
              1255,
              1315,
              1335,
              1355,
              1415,
              1435,
              1455,
              1515,
              1535,
              1555,
              1615,
              1635,
              1655,
              1715,
              1735,
              1755,
              1815,
              1835,
              1855,
              1915,
              1935,
              1955,
              2015,
              2035,
              2055,
              2115,
              2135,
              2155,
              2215,
              2245,
              2320,
              2355
            ],
            "JSQ_HOB_33S": [
              10,
              30,
              50,
              110,
              130,
              150,
              210,
              250,
              330,
              410,
              450,
              530,
              610,
              650,
              730,
              810,
              845,
              920,
              940,
              1000,
              1012,
              1024,
              1036,
              1048,
              1100,
              1112,
              1124,
              1136,
              1148,
              1200,
              1212,
              1224,
              1236,
              1248,
              1300,
              1312,
              1324,
              1336,
              1348,
              1400,
              1412,
              1424,
              1436,
              1448,
              1500,
              1510,
              1520,
              1530,
              1540,
              1550,
              1600,
              1610,
              1620,
              1630,
              1640,
              1650,
              1700,
              1710,
              1720,
              1730,
              1740,
              1750,
              1800,
              1810,
              1820,
              1830,
              1840,
              1850,
              1900,
              1910,
              1920,
              1930,
              1940,
              1950,
              2000,
              2015,
              2035,
              2055,
              2115,
              2135,
              2155,
              2215,
              2235,
              2255,
              2315,
              2335
            ],
            "33S_HOB_JSQ": [
              8,
              28,
              48,
              108,
              128,
              148,
              208,
              228,
              248,
              328,
              408,
              448,
              528,
              608,
              648,
              728,
              808,
              848,
              923,
              958,
              1018,
              1038,
              1050,
              1102,
              1114,
              1126,
              1138,
              1150,
              1202,
              1214,
              1226,
              1238,
              1250,
              1302,
              1314,
              1326,
              1338,
              1350,
              1402,
              1414,
              1426,
              1438,
              1450,
              1502,
              1514,
              1526,
              1538,
              1548,
              1558,
              1608,
              1618,
              1628,
              1638,
              1648,
              1658,
              1708,
              1718,
              1728,
              1738,
              1748,
              1758,
              1808,
              1818,
              1828,
              1838,
              1848,
              1858,
              1908,
              1918,
              1928,
              1938,
              1948,
              1958,
              2008,
              2018,
              2028,
              2038,
              2053,
              2113,
              2133,
              2153,
              2213,
              2233,
              2253,
              2313,
              2333,
              2353
            ]
          }
        }
      ],
      "timings": [
        {
          "scheduleId": 1,
          "startDay": 1,
          "startTime": 0,
          "endDay": 6,
          "endTime": 0
        },
        {
          "scheduleId": 2,
          "startDay": 6,
          "startTime": 0,
          "endDay": 7,
          "endTime": 0
        },
        {
          "scheduleId": 2,
          "startDay": 7,
          "startTime": 0,
          "endDay": 1,
          "endTime": 0
        }
      ],
      "name": "Regular"
    }

""".trimIndent()