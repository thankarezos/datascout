import requests 
import json
from PIL import Image
import io
import numpy as np

def main():
    files = {
        'uri' :"https://media.discordapp.net/attachments/1079599772287127653/1191881082375770112/415059350_390596366697355_6342356019264221322_n.png?ex=65a70cc2&is=659497c2&hm=29163c39279e10fbae2a9a6a16e293e10cace0da51e8fe5eeb7527a7696f4554&=&format=webp&quality=lossless&width=475&height=594"
    }

    res = requests.post('http://localhost:3333/infer',data=files).text
    print(res)
    annt_f = {
        'uri':"https://media.discordapp.net/attachments/1079599772287127653/1191881082375770112/415059350_390596366697355_6342356019264221322_n.png?ex=65a70cc2&is=659497c2&hm=29163c39279e10fbae2a9a6a16e293e10cace0da51e8fe5eeb7527a7696f4554&=&format=webp&quality=lossless&width=475&height=594"
        ,'scores': json.dumps(json.loads(res))
    }
    img_b = requests.post('http://localhost:3333/annt',data=annt_f,stream=True)
    
    image = Image.open(io.BytesIO(img_b.content))
    image.show()


if __name__ == '__main__':
    main()