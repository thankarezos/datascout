import React from "react";
import { InboxOutlined } from "@ant-design/icons";
import type { UploadProps } from "antd";
import { message, Upload } from "antd";

const { Dragger } = Upload;

//get request to http://localhost:8080/test

// const get = async () => {
//     const response = await fetch("http://localhost:8080/test");
//     const data = await response.json();
//     console.log(data);
// };

const props: UploadProps = {
  name: "file",
  multiple: true,
  action: "http://localhost:8080/upload",
  onChange(info) {
    const { status } = info.file;
    console.log(info.file);
    if (status !== "uploading") {
      console.log(info.file, info.fileList);
    }
    if (status === "done") {
      message.success(`${info.file.name} file uploaded successfully.`);
    } else if (status === "error") {
      message.error(`${info.file.name} file upload failed.`);
    }
  },
  onDrop(e) {
    console.log("Dropped files", e.dataTransfer.files);
  },
};

const UploadPage: React.FC = () => (
  <>
    <Dragger {...props}>
      <p className="ant-upload-drag-icon">
        <InboxOutlined />
      </p>
      <p className="ant-upload-text">
        Click or drag file to this area to upload
      </p>
      <p className="ant-upload-hint">
        Support for a single or bulk upload. Strictly prohibited from uploading
        company data or other banned files.
      </p>
    </Dragger>
  </>
);

export default UploadPage;
