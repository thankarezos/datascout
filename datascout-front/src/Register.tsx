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
    email: string;
    password: string;
}

interface Error{
  error: string;
  success : boolean
}


const NormalRegisterForm = () => {
  const navigate = useNavigate();
  const [error, setError] = useState<Error>({error: '', success: false});
  const [form] = Form.useForm();

  const compareToFirstPassword = (_: unknown, value: string) => {
    if (value && value !== form.getFieldValue('password')) {
      // Directly return the message string with Promise.reject
      return Promise.reject('The two passwords that you entered do not match!');
    }
    return Promise.resolve();
  };
  

  const onFinish = (values: LoginCredentials) => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    console.log('Received values of form: ', values);
    axios.post('/api/register', values)
    .then(response => {
      //go to user page
      navigate('/login');
      console.log(response);
    })
    .catch(error => {
      console.log(error);
      setError({error: error.response?.data?.error, success: false});
    });
  };

  return (
    <div className="login-form-container">
      <Form
        form={form}
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
          name="email"
          rules={[
            {
              required: true,
              message: 'Please input your Email!',
            },
          ]}
        >
          <Input prefix={<UserOutlined className="site-form-item-icon" />} placeholder="Email" />
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

        <Form.Item
        name="confirm"
        dependencies={['password']}
        hasFeedback
        rules={[
          {
            required: true,
            message: 'Please confirm your password!',
          },
          {
            validator: compareToFirstPassword,
          },
        ]}
      >
        <Input
          prefix={<LockOutlined className="site-form-item-icon" />}
          type="password"
          placeholder="Confirm Password"
        />
      </Form.Item>
        
        {/* Display error message if password is wrong */}
        <div id="error-message" style={{ color: 'red', marginBottom: '10px' }}>
          {error.error}
        </div>

        <Form.Item>
          <Button type="primary" htmlType="submit" className="login-form-button">
            Register
          </Button>
            Or <a onClick={() => navigate('/login')}>login now!</a>
        </Form.Item>
      </Form>
    </div>
  );
};


export default NormalRegisterForm;
