import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
// import "antd/dist/reset.css";
import "./index.css";
import { Upload, Modal, Form, UploadFile, Button } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import axios from "axios";

interface Image {
    id: number;
    userId: number;
    path: string;
    labels: Label[];
}

interface Label {
    label: string;
    count: number;
}

async function getBase64(file: Blob) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => {
            if (reader.result) {
                resolve(reader.result as string);
            } else {
                reject(new Error("Failed to read file."));
            }
        };
        reader.onerror = error => reject(error);
    }) as Promise<string>;
}


const logout = () => {
    // Remove JWT from local storage
    localStorage.removeItem('jwt'); // Change 'jwtToken' to the key you used to store the JWT
    // Redirect to the login page or perform any other necessary action
    // For example, you can use React Router to navigate to the login page
    // Example using React Router:
    window.location.href = '/login'; // Redirect to the login page
};


const PicturesWall: React.FC = () => {
    const [previewVisible, setPreviewVisible] = useState(false);
    const [previewImage, setPreviewImage] = useState("");
    const [previewImageAnnotated, setPreviewImageAnnotated] = useState("");
    const [currentLabels, setCurrentLabels] = useState<Label[]>([]);
    const [fileList, setFileList] = useState<UploadFile<unknown>[]>([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
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
                            url: `/api/image/${image.id}`,
                            labels: image.labels.map(label => {
                                return {
                                    label: label.label,
                                    count: label.count
                                };
                            })
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

        if (!loading) {
            getImages();
        }

    }, [loading, navigate]);

    useEffect(() => {
        if (fileList.some(file => file.status === "uploading")) {
            setLoading(true);
        }
        else {
            setLoading(false);
        }
    }, [fileList]);





    const handleCancel = () => setPreviewVisible(false);

    const handlePreview = async (file: UploadFile<unknown>) => {
        if (!file.url && !file.preview) {
            file.preview = await getBase64(file.originFileObj as Blob);
        }

        // Assuming `file.url` is the path to the non-annotated image
        setPreviewImage(file.url || file.preview);
        console.log(file.labels);

        // Construct the URL for the annotated image. Adjust according to your API.
        // For example, if the annotated image is accessed via a query parameter:
        const annotatedUrl = file.url ? `${file.url}?annotated=true` : '';
        setPreviewImageAnnotated(annotatedUrl);

        if (file.labels) {
            setCurrentLabels(file.labels);
        }

        setPreviewVisible(true);
    };

    const deleteImage = async (fileId: number) => {
        await axios.post(`/api/image/${fileId}`);
    };



    const handleChange = ({ file, fileList }: { file: UploadFile, fileList: UploadFile<unknown>[] }) => {
        // If the file was removed, call the delete function
        if (file.status === 'removed') {
            // Call the deleteImage function and wait for the promise
            deleteImage(file.uid as number) // Assuming the file.uid is the image id, cast as number if needed
                .then(() => {
                    // If delete is successful, update the fileList state to exclude the deleted file
                    console.log("Image deleted");
                    setFileList(fileList.filter(f => f.uid !== file.uid));
                })
                .catch(() => {
                    // If there's an error, log it and don't remove the file from the list
                    console.error("Error deleting image");
                    // Optionally, you might want to inform the user or handle the error visually
                });
        } else {
            // If the file status is not 'removed' (e.g., 'done', 'uploading', etc.), just update the fileList
            setFileList(fileList);
        }
    };

    const uploadButton = (
        <div>
            <PlusOutlined />
            <div className="ant-upload-text">Upload</div>
        </div>
    );

    return (
        <>
           <div style={{ position: 'absolute', top: 10, right: 10 }}>
            <Button type="primary" onClick={logout}>Logout</Button>
            </div>
            <Form layout="vertical" className="image-upload-form">
                <Form.Item className="image-upload-form-item">
                    <Upload
                        action='/api/upload'
                        listType="picture-card"
                        fileList={fileList}
                        onPreview={handlePreview}
                        onChange={handleChange}
                        className={"customSizedUpload"}
                    >
                        {uploadButton}
                    </Upload>
                    <Modal visible={previewVisible} footer={null} onCancel={handleCancel} width="80%">
                        <div className="preview-image-container">
                            <div className="preview-image">
                                <img alt="Non-Annotated example" style={{ width: "100%" }} src={previewImage} />
                            </div>
                            <div className="preview-image">
                                <img alt="Annotated example" style={{ width: "100%" }} src={previewImageAnnotated} />
                            </div>
                        </div>

                        <div className="label-container">
                            <div className="labels-titles">
                                <p>Label</p>
                                <p>Count</p>
                            </div>

                            {currentLabels.map((label, index) => (
                                <div className="inside-label" key={index}>
                                    <p>{label.label}</p>
                                    <p>{label.count}</p>
                                </div>
                            ))}
                        </div>
                    </Modal>
                </Form.Item>
            </Form>
        </>
    );
};

export default PicturesWall;
