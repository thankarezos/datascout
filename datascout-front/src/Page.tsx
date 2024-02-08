import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
// import "antd/dist/antd.css";
import "./index.css";
import { Upload, Modal, Form, UploadFile } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import axios from "axios";

interface Image {
        id: number;
        userId: number;
        path: string;
        label: Label[];
}

interface Label {
        label: string;
        count: number;
}

function getBase64(file: unknown) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file as Blob);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}



const PicturesWall: React.FC = () => {
    const [previewVisible, setPreviewVisible] = useState(false);
    const [previewImage, setPreviewImage] = useState("");
    const [fileList, setFileList] = useState<UploadFile<unknown>[]>([]);
    const navigate = useNavigate();

    useEffect(() => {
        getImages();
    } , []);

    const getImages = () => {
        axios.get('/api/images')
        .then(response => {
            const images: Image[] = response.data.data;
            //if authorized go to login page
            
            
            
            setFileList(images.map(image => {
                return {
                    uid: image.id.toString(),
                    name: image.path,
                    status: "done",
                    url: `/api/image/${image.id}/annotated=true`
                };
            }));
            
        })
        .catch(error => {
            if (error.response.status === 401) {
                navigate("/auth");
            }
            console.log(error);
            
        });
    };

    const handleCancel = () => setPreviewVisible(false);

    const handlePreview = async (file: { url: unknown; preview: unknown; originFileObj: unknown }) => {
        if (!file.url && !file.preview) {
            file.preview = await getBase64(file.originFileObj);
        }

        // setPreviewImage(file.url || file.preview);
        setPreviewVisible(true);
    };

    const handleChange = ({ fileList }: { fileList: UploadFile<unknown>[] }) => setFileList(fileList);

 

    const uploadButton = (
        <div>
            <PlusOutlined />
            <div className="ant-upload-text">Upload</div>
        </div>
    );

    return (
        <>
            <Form layout="vertical">
                <Form.Item label="Images">
                    <Upload
                        action="/api/upload"
                        listType="picture-card"
                        fileList={fileList}
                        onPreview={handlePreview}
                        onChange={handleChange}
                    >
                        {fileList.length >= 30 ? null : uploadButton}
                    </Upload>
                    <Modal visible={previewVisible} footer={null} onCancel={handleCancel}>
                        <img alt="example" style={{ width: "100%" }} src={previewImage} />
                    </Modal>
                </Form.Item>
            </Form>
            <div>Content below</div>
        </>
    );
};

export default PicturesWall;
