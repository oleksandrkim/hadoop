"""Extracting historical weather data from darksky api; 
Data is stored in JSON format for further processing in mapreduce"""

import requests
import time

Path("[insert directory here]").touch() #creates a text file to store data

#API parameters
params = (
    ('exclude', 'currently,flags,minutely,hourly,alerts'),
    ('units', 'si'),
)

#timestamp = insert time stamp here, unix (1451649600)
 

#
#API_key = insert dark sky api key here
#lat = insert lat of a desired location here
#lon = insert lon of a desired location here
#day_num - insert number of days to extract info about

for i in range(365):
    print("Iteration: "+str(day_num))
    request = 'https://api.darksky.net/forecast/'+API_key+'/'+lat+','+lon,'+str(timestamp)
    response = requests.get(request, params=params)
    data = response.json()
    f.write(str(data['daily']['data'][0]))
    f.write("\n")
    timestamp = timestamp+86400					#adds one day to a timestamp
    if i % 5 == 0:								#making a pause for 3 seconds every 5th day
        time.sleep(3)


f.close() 
