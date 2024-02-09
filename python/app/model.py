from __future__ import annotations
from transformers import DetrImageProcessor, DetrForObjectDetection
import torch
from PIL import Image
from PIL import ImageDraw,ImageFont
import requests
import json
import numpy as np
from fastapi import FastAPI, Form, UploadFile
from fastapi.responses import Response, StreamingResponse

from typing_extensions import Annotated
import uvicorn
from typing import Optional 
import io

app = FastAPI()

class Model:
    __model = None
    __processor = None 

    @staticmethod
    def id2label(label : int) ->str:
        return Model.__model.config.id2label[label]
    
    @staticmethod
    def load() -> None:
        if Model.__processor is None:
          Model.__processor = DetrImageProcessor.from_pretrained("facebook/detr-resnet-50")
        if Model.__model is None:
         Model.__model = DetrForObjectDetection.from_pretrained("facebook/detr-resnet-50")

    @staticmethod
    def predict(image) -> dict:
       assert Model.__processor is not None and Model.__model is not None
       inputs = Model.__processor(images=image, return_tensors="pt")
       outputs = Model.__model(**inputs)
       target_sizes = [ img.size[::-1] for img in image ]
       #why do these tensors require gradients????
       return Model.__processor.post_process_object_detection(outputs, target_sizes=target_sizes, threshold=0.9)


def from_url(url) -> Image:
        return Image.open(requests.get(url,stream=True).raw).convert('RGB')

def dump_bytes(im : Image)->str:
    image_bytes_io = io.BytesIO()
    im.save(image_bytes_io,format='PNG') 
    return image_bytes_io.getvalue()


def to_bytes(t : torch.tensor)->str:
    return t.detach().cpu().numpy().tolist()

def from_bytes(bytes : str | bytes | bytearray)->Image:
    return Image.fromarray(np.array(json.loads(bytes),dtype='uint8')).convert('RGB')

def filter(results : dict , whitelist : set = None) -> dict:
    if whitelist is None:
        return results
    out =  {"scores":[],'labels':[],'boxes':[]}
    any_ : bool = False
    for score , label , box in zip(results['scores'],results['labels'],results['boxes']): 
        if Model.id2label(label.item()) in whitelist:
            any_ = True
            out["scores"] .append(score)
            out["labels"] .append(label)
            out["boxes"]  .append(box)
            
    return out if any_ else {}

def clamp_coordinates(x, y, width, height):
    if x < 0:
        x = 0
    elif x >= width:
        x = width - 1
    if y < 0:
        y = 0
    elif y >= height:
        y = height - 1

    return x, y

def annotate(results :dict,image:Image) ->None:
       draw = ImageDraw.Draw(image)
       for score, label, box in zip(results["scores"], results["labels"], results["boxes"]):
          box = [round(i, 2) for i in box]
          draw.rectangle(box,outline='lightgreen',width=2)
          text = f"{label}: {(score*100):.2f}%"
          text_bbox = draw.textbbox((0, 0), text)
          text_width, text_height = text_bbox[2] - text_bbox[0], text_bbox[3] - text_bbox[1]
          text_x = (box[0] + box[2] - text_width) // 2
          text_y = box[1] - text_height - 5 
          image_height , image_width = image.size
          text_x,text_y = clamp_coordinates(text_x,text_y,image_height,image_width)
          draw.text((text_x, text_y), text, fill="#00ff00")   

def test():
   url="https://media.discordapp.net/attachments/1079599772287127653/1191881082375770112/415059350_390596366697355_6342356019264221322_n.png?ex=65a70cc2&is=659497c2&hm=29163c39279e10fbae2a9a6a16e293e10cace0da51e8fe5eeb7527a7696f4554&=&format=webp&quality=lossless&width=475&height=594"
   image = from_url(url) 
   image = from_bytes(dump_bytes(image))
   Model.load()
   res = Model.predict(image)
   res = filter(res,'couch')
   annotate(res,image)
   
   image.show()


    
@app.post("/annt")
async def antt(scores: Annotated[str, Form()],file: Annotated[UploadFile, Form()] = None  , uri: Annotated[str,Form()] = None):
   
    if file is not None:
     contents = await file.read()
     image = Image.open(io.BytesIO(contents)).convert('RGB')
    else :
        assert uri is not None
        image = from_url(uri)
    scores = json.loads(scores)
    annotate(dict(scores),image)
    return StreamingResponse( io.BytesIO(dump_bytes(image)) , media_type="image/png")

@app.post("/infer")
async def root(file : Annotated[list[UploadFile],Form()] = None , 
               uri : Annotated[str,Form()] = None,
               whitelist : Annotated[str,Form()] = None):
    images = []
    if file is not None:
     for f in file:
        contents = await f.read()
        image = Image.open(io.BytesIO(contents)).convert('RGB')
        images.append(image)

    if uri:
        uri = uri.replace('{','').replace('}','').replace('[','').replace(']','').split(',')
        images += [from_url(u) for u in uri]

    pred = Model.predict(images)
    if whitelist:
        whitelist = whitelist.replace('{','').replace('}','').replace('[','').replace(']','').split(',')
        s = set(whitelist)
        newpreds = []
        for i in range(len(pred)):
            f = filter(pred[i],s)
            if len(f):
                newpreds.append(f)
        pred = newpreds
    ret = [{ k : [to_bytes(v_) for v_ in v] for k,v  in p.items()} for p in pred]
    for i in range(len(ret)):
        ret[i]["labels"] = [Model.id2label(s) for s in ret[i]["labels"]]

    if len(ret) == 1:
        return ret[0]
    else:
        return {"data":ret}

if __name__ =='__main__':
    Model.load()
    uvicorn.run(app, host="0.0.0.0", port=3333)