from __future__ import annotations
from transformers import DetrImageProcessor, DetrForObjectDetection
import torch
from PIL import Image
from PIL import ImageDraw,ImageFont
import requests
import json
import numpy as np
from fastapi import FastAPI
import uvicorn
from pydantic import BaseModel
from typing import Optional

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
    def predict(image:Image) -> dict:
       assert Model.__processor is not None and Model.__model is not None
       inputs = Model.__processor(images=image, return_tensors="pt")
       outputs = Model.__model(**inputs)
       target_sizes = torch.tensor([image.size[::-1]])
       return Model.__processor.post_process_object_detection(outputs, target_sizes=target_sizes, threshold=0.9)[0]


def from_url(url) -> Image:
        return Image.open(requests.get(url,stream=True).raw)

def dump_bytes(im : Image)->str:
    return json.dumps(np.array(im).tolist())


def to_bytes(t : torch.tensor)->str:
    return json.dumps(t.detach().cpu().numpy().tolist())

def from_bytes(bytes : str | bytes | bytearray)->Image:
    return Image.fromarray(np.array(json.loads(bytes),dtype='uint8'))

def filter(results : dict , whitelist : set = None) -> dict:
    if whitelist is None:
        return results
    out:dict = {'scores':[],'labels':[],'boxes':[]}
    for score , label , box in zip(results['scores'],results['labels'],results['boxes']): 
        if Model.id2label(label.item()) in whitelist:
            out["scores"].append(score)
            out["labels"].append(label)
            out["boxes"].append(box)
    return out

def annotate(results :dict,image:Image) ->None:
       draw = ImageDraw.Draw(image)
       for score, label, box in zip(results["scores"], results["labels"], results["boxes"]):
          box = [round(i, 2) for i in box.tolist()]
          draw.rectangle(box,outline='lightgreen',width=2)
          text = f"{Model.id2label(label.item())}: {round(score.item(),3)*100}%"
          text_width, text_height = draw.textsize(text)
          text_x = (box[0] + box[2] - text_width) // 2
          text_y = box[1] - text_height - 5 
          font = ImageFont.truetype("arial.ttf", 16)
          draw.text((text_x, text_y), text, fill="#00ff00",font=font)   

def test():
   url="https://media.discordapp.net/attachments/1079599772287127653/1191881082375770112/415059350_390596366697355_6342356019264221322_n.png?ex=65a70cc2&is=659497c2&hm=29163c39279e10fbae2a9a6a16e293e10cace0da51e8fe5eeb7527a7696f4554&=&format=webp&quality=lossless&width=475&height=594"
   image = from_url(url) 
   image = from_bytes(dump_bytes(image))
   Model.load()
   res = Model.predict(image)
   res = filter(res,'couch')
   annotate(res,image)
   
   image.show()


class Request(BaseModel):
    raw_imgs : Optional[list[bytes]] | Optional[list[str]] = None
    urls : Optional[list[str]] = None
    whitelist : Optional[list[str]] = []
    do_annotate : Optional[bool] = False


@app.get("/infer")
async def root(req : Request ):
    images = []
    if req.raw_imgs is not None:
        images += [from_bytes(s) for s in req.raw_imgs]
    if req.urls is not None:
        assert req.urls is not None
        images += [from_url(u) for u in req.urls]

    pred = Model.predict(images)
    if req.whitelist:
        s = set(req.whitelist)
        pred = [ filter(p, s)  for  p  in pred ]

    print(pred)

    resp = {"results":[{k,to_bytes(v)} for k,v in pred.items()]}
    
    if req.do_annotate:
        resp["ann"] = [None]* len(images)
        for i,image in enumerate(images):
            annotate(pred,image)
            resp["ann"][i] = dump_bytes(image)
    return resp

if __name__ =='__main__':
    Model.load()
    uvicorn.run(app,host="0.0.0.0", port=3333)