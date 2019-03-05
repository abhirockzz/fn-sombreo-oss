# Serverless website example using Fn and OCI Object Storage

To start with, clone this repo and change into the correct directory - `cd fn-sombrero-oss`

## Fn setup

### Start Fn server

`fn start &` - this will start the `fnproject/fnserver` docker container and expose port `8080` to your docker host

## Deploy function to Fn server

- `cd sombrero-function`
- `fn use context default`
- `fn create app sombrero`
- `fn -v deploy --app sombrero --local`

## nginx set up

ngnix is being used as proxy in front of fnserver for SSL etc. Please ensure that you're able to expose nginx publiclye.g. by running it in VM or using `ngrok`. The website will point to the nginx `ip:port` (details in next section).

For SSL, it simple uses self-signed certificates (this is just a demo!)

To run nginx in docker,

- `cd nginx-ssl-proxy`
- `docker run --link fnserver --name nginx_proxy -d -v `pwd`:/etc/nginx/conf.d -p 443:443 nginx`

## Front end

### Update

Update line `252` in `sombrero.html` with your ngnix IP and the function ID

### Upload to OCI Object Storage

You need to place the front end assets in a OCI Storage Bucket - in this case, its simply the `sombrero.html`. Create a pre-authenticated request for the object and get the URL and save it. To access the website, simply use the auto-generated URL 

## Test

Since we have use self-signed certificate in ngnix, you first need to add this is as an exception in your browser. Just access the hostname of the VM where nginx docker container is running i.e. `https://<hostname-0f-nginx-vm>` and accept the exception. It will forward request to Fn server and you should get back this JSON - `{"goto":"https://github.com/fnproject/fn","hello":"world!"}`

You can now access UI, click a picture using the web camera and let serverless function `sombrerofy` you!
