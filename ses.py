#!/usr/bin/python

import sys
import boto3

client = boto3.client('ses', region_name='us-west-2')
response = client.send_email(
    Source='EC2 Snapshot <craigjk@cox.net>',
    Destination={
        'ToAddresses' : [
            'craigjk@cox.net'
        ]
    },
    Message={
        'Subject' : {
            'Data' : 'last captured file too old'
        },
        'Body' : {
            'Text' : {
                'Data' : sys.argv[1]
            }
        }
    }
)
