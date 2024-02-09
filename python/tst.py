import requests 
import json
from PIL import Image
import io
import numpy as np

URI = "https://image.cnbcfm.com/api/v1/image/106947859-GettyImages-167503543.jpg?v=1632760991"
#"https://media.istockphoto.com/id/155287967/photo/cars-in-rush-hour-with-traffic-at-dawn.jpg?s=612x612&w=0&k=20&c=tDAaJQMHIh6nFDr0rJlD44tEtmj2srdpoSTKL7C5SGE="
#"https://media.discordapp.net/attachments/1079599772287127653/1191881082375770112/415059350_390596366697355_6342356019264221322_n.png?ex=65a70cc2&is=659497c2&hm=29163c39279e10fbae2a9a6a16e293e10cace0da51e8fe5eeb7527a7696f4554&=&format=webp&quality=lossless&width=475&height=594"

def test_uri():
    files = {
        'uri' :URI
    }

    res = requests.post('http://localhost:3333/infer',data=files).text
    print(res)
    annt_f = {
        'uri':URI ,'scores': json.dumps(json.loads(res)[0])
    }
    img_b = requests.post('http://localhost:3333/annt',data=annt_f,stream=True)
    
    image = Image.open(io.BytesIO(img_b.content))
    image.show()

def test_whitelist():
    files = {
        'whitelist' : ['couch'],
        'uri' : URI
    }

    res = requests.post('http://localhost:3333/infer',data=files).text
    print(res)
    annt_f = {
        'uri': URI ,'scores': json.dumps(json.loads(res)[0])
    }
    img_b = requests.post('http://localhost:3333/annt',data=annt_f,stream=True)
    
    image = Image.open(io.BytesIO(img_b.content))
    image.show()

def test_multi():
    STOP = 'https://www.ilankelman.org/stopsigns/austria.jpg'
    files = {
        'whitelist' : ','.join(['couch','stop sign', 'car']),
        'uri' : ','.join([URI , STOP])
    }
        
    res = requests.post('http://localhost:3333/infer',data=files).text
    print(res)
    annt_f = {
        'uri':URI ,'scores': json.dumps(json.loads(res)['data'][0])
    }
    img_b = requests.post('http://localhost:3333/annt',data=annt_f,stream=True)
    
    image = Image.open(io.BytesIO(img_b.content))
    image.show()


if __name__ == '__main__':
    #test_uri()
    #test_whitelist()
    test_multi()