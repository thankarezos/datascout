// import 'antd/dist/reset.css';
import './index.css';
import { Form, Input, Button } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

//login model
interface LoginCredentials {
    username: string;
    password: string;
}

interface Error{
  error: string;
  success : boolean
}

const ErrorNi = (err : Error) => {
  document.getElementById('error-message').innerText = error.erorr; // Set error message text
  document.getElementById('error-message').style.visibility = 'visible'; // Set visibility to visible
};

const NormalLoginForm = () => {
  const navigate = useNavigate();

  const onFinish = (values: LoginCredentials) => {
    axios.post('/api/login', values)
    .then(response => {
      if( 'error' in response){
        ErrorNi(response as Error)
        return;
      }
      //go to user page
      navigate('/');
      console.log(response);
    })
    .catch(error => {
      console.log(error);
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
        <div id="error-message" style={{ visibility: 'hidden', color: 'red', marginBottom: '10px' }}>
          {/* Error message will be set dynamically */}
        </div>

        <Form.Item>
          <Button type="primary" htmlType="submit" className="login-form-button">
            Log in
          </Button>
          Or <a onclick="navigate('/register')">register now!</a>
        </Form.Item>
      </Form>
    </div>
  );
};


const NormalRegisterForm = () => {
  const navigate = useNavigate();

  const onFinish = (values: LoginCredentials) => {
    axios.post('/api/register', values)
    .then(response => {
      if('error' in response){
        ErrorNi(response as Error)
        return;
      }
      //go to user page
      navigate('/login');
      console.log(response);
    })
    .catch(error => {
      console.log(error);
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
        <div id="error-message" style={{ visibility: 'hidden', color: 'red', marginBottom: '10px' }}>
          {/* Error message will be set dynamically */}
        </div>

        <Form.Item>
          <Button type="primary" htmlType="submit" className="login-form-button">
            Register
          </Button>
          Or <a onclick="navigate('/login')">login now!</a>
        </Form.Item>
      </Form>
    </div>
  );
};


export default NormalLoginForm;
