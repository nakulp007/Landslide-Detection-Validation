#!/usr/bin/env python
# -*- coding: utf-8 -*-

import json
from pprint import pprint
import os
import sys 
import time 
import datetime

from geopy.geocoders import GoogleV3
geolocator = GoogleV3()

landslides_list = []

wanted_keys = ['start', 'end', 'title', 'point', 'options']
for fn in os.listdir('.'):
     if os.path.isfile(fn):
        extension = os.path.splitext(fn)[1]
        if extension == '.txt':
            with open(fn) as data_file:    
                data = json.load(data_file)
                for data_item in data:
                    #Format Date
                    try:
                        date = datetime.datetime.strptime(data_item['pubDate'], '%b %d, %Y %H:%M:%S %p').strftime("%Y-%m-%d")
                    except:
                        #print "pubDate: " + data_item['pubDate']
                        continue
                    options = {}
                    options['news_title'] = data_item['title']
                    options['description'] = data_item['description']
                    options['link'] = data_item['link']
                    
                    #Get Geo-coordinates
                    if 'cities' in data_item.keys():
                        cities = data_item['cities']
                        if len(cities) == 0:
                            continue

                        main_city = ""
                        relevance = 0;
                        point = {}
                        for city in cities:
                            if city['relevance'] > relevance:
                                relevance = city['relevance']
                                main_city = city
                                options['city'] = main_city['name'].title()
                                data_item['title'] = options['city']
                        

                        if 'geoLocation' in main_city.keys():
                            points = main_city['geoLocation'].split()
                            lat = points[0]
                            lon = points[1]
                            point['lat'] = float(lat)
                            point['lon'] = float(lon)
                            print point
                        else:
                            location = geolocator.geocode(main_city['name'], timeout=60)
                            if location is None:
                                continue
                            print [location.latitude, location.longitude]
                            
                            point['lat'] = location.latitude
                            point['lon'] = location.longitude
                            
                        data_item['point'] = point

                        reverse = geolocator.reverse([point['lat'], point['lon']],1)
                        
                        if reverse is None:
                            options['city'] = main_city['name'].title()
                            data_item['title'] = options['city']
                        else:
                            address_components = reverse.raw['address_components']
                            for item in address_components:
                                item_set = set(item['types'])
                                city_set = set(['locality', 'political'])
                                country_set = set([ "country", "political" ])
                                if item_set == city_set:
                                    options['city'] = item['long_name']
                                if item_set == country_set:
                                    options['country']= item['long_name']
                            try:
                                data_item['title'] = options['city'] + "," + options['country']
                            except:
                                data_item['title'] = options['country']

                    for key in data_item.keys():
                        if key not in wanted_keys:
                            data_item.pop(key)

                    data_item['options'] = options
                    data_item['start'] = date
                    data_item['end'] = date
                    print data_item['title']
                    landslides_list.append(data_item)
                    time.sleep(1)

with open('data.txt', 'w') as outfile:
    json.dump(landslides_list, outfile)

print len(landslides_list)
pprint(landslides_list[3])




#for data_item in data:
    #data_item['title']
    #data_item['link']
    #data_item['pubDate']

    #data_item['cities']

#pprint(data)