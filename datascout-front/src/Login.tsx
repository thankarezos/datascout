// import 'antd/dist/reset.css';
import './index.css';
import { Form, Input, Button } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';

//login model
interface LoginCredentials {
    username: string;
    password: string;
}

interface Error{
  error: string;
  success : boolean
}


const NormalLoginForm = () => {
  const navigate = useNavigate();
  const [error, setError] = useState<Error>({error: '', success: false});

  const onFinish = (values: LoginCredentials) => {
    axios.post('/api/login', values)
    .then(response => {
      //go to user page
      navigate('/');
      console.log(response);
    })
    .catch(error => {
      setError({error: error.response.data.error, success: false});
      // error = error.response.error
      // console.log(error);
    });
  };

  return (
    <div className="login-form-container">
      <Form
        name="normal_login"
        className="login-form"
        initialValues={{
          remember: true,
        }}
        onFinish={onFinish}
      >
        <Form.Item
          name="username"
          rules={[
            {
              required: true,
              message: 'Please input your Username!',
            },
          ]}
        >
          <Input prefix={<UserOutlined className="site-form-item-icon" />} placeholder="Username" />
        </Form.Item>
        <Form.Item
          name="password"
          rules={[
            {
              required: true,
              message: 'Please input your Password!',
            },
          ]}
        >
          <Input
            prefix={<LockOutlined className="site-form-item-icon" />}
            type="password"
            placeholder="Password"
          />
        </Form.Item>
        {/* Display error message if password is wrong */}
        <div id="error-message" style={{ color: 'red', marginBottom: '10px' }}>
          {error.error}
        </div>

        <Form.Item>
          <Button type="primary" htmlType="submit" className="login-form-button">
            Log in
          </Button>
          Or <a onClick={() => navigate('/register')}>register now!</a>
        </Form.Item>
      </Form>
    </div>
  );
};


export default NormalLoginForm;
