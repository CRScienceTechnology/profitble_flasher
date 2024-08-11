//nodejs v18.20.3

const express = require('express');        // express 变量实际上持有的是 createApplication 函数的引用
const bodyParser = require('body-parser');    // json解析中间间的引用
const fs = require('fs');                     // 文件操作库引用
const path = require('path');                 // 类似python的import
const { exec } = require('child_process');    // 只从child_process中导入exec函数，类似Py的from import 

const app = express();                        // 使用 require 返回的函数创建一个新的 Express 应用实例
                                              // https://kimi.moonshot.cn/sharecqron62tnn0mhpkc2j80 
const PORT = process.env.PORT || 3001;        // 可以看成能返回结构体的函数，这个函数成员执行的更多的是
                                              // 实际性的功能   
app.use(bodyParser.json());

const source_code_folder = '/root/REMOTE_COMPILER/compile_folder/source_code_folder';
const output_folder = '/root/REMOTE_COMPILER/compile_folder/output_folder';

let source_code
let sendData
let source_code_name
// 处理客户端post请求，req是服务器接收到的请求体，res是服务器发送的请求体
// 实际上如果不开uginx服务，直接访问外部地址+uri描述符会被路由到localhost:3001端口
app.post('/api/compile_server', async (req, res) => {
    try {
        sendData = req.body; //解析http的请求体，请求体已经过bodyParser.json()中间件处理
        source_code_name = sendData.name; // 访问json中name键
        source_code = sendData.code;      // 访问json中的代码本体
        console.log('Request received'); 
        console.log('CodeName:',sendData.name);
        console.log('Received data:', sendData);

        

        if (!source_code_name) {
            return res.status(400).send({ error: 'Source code not provided' });
        }
        
        
        

        // 将源代码写入文件
        const sourceFilePath = path.join(source_code_folder, `${source_code_name}.c`); //调用join连接路径字符
        fs.writeFileSync(sourceFilePath, source_code, 'utf8');
        // 此处编译是顺序编译，所以回调函数要依次嵌套
        // 执行编译命令
       
        exec(`sdcc '${sourceFilePath}' -o '${output_folder}/'`, (error) =>
        // 将源代码生成ihx文件 相当于在终端执行sdcc '/root.../main.c' -o '/root...foleder/'
        {
            if (error) {
                console.error(`Compilation failed: ${error.message}`);
                return res.status(500).send({ error: 'Compilation failed' });
            }
            console.log("SDCC OK!")
            // 编译成功后，执行packihx命令
            // sdcc命令直接输出的是ihx文件，此处调用packihx将文件转换为hex,$符号作为转义符
            // 相当于在终端执行packihx '/root.../main.ihx' > '/root../main.hex',``表示命令行环境字符  
            const packihxCommand = `packihx '${path.join(output_folder, `${source_code_name}.ihx`)}' > '${path.join(output_folder, `${source_code_name}.hex`)}'`;
            exec(packihxCommand, (err) => 
            {
                if (err) 
                {
                    console.error(`Error converting .ihx to .hex: ${err.message}`);
                    return res.status(500).send({ error: 'Conversion failed' });
                }
                console.log("ihx2hex OK!")
                // 读取并发送.hex文件
               ;
            });
        });
    } catch (err) {
        console.error(err);
        return res.status(500).send({ error: 'An error occurred' });
    }
});

app.get('/api/compile_server',async(req,res)=>{
    try {
        fs.readFile(path.join(output_folder, `${source_code_name}.hex`), 'utf8', (readErr, data) => 
            {
                console.log("回传数据:"+String(data))
                if (readErr) 
                {
                    console.error(`Error reading .hex file: ${readErr.message}`);
                    return res.status(500).send({ error: 'File read failed' });
                }
                res.status(200).json({
                    name:source_code_name,  
                    hex: data,
                    filetype:'hex' }); // 回传json
                })
                
                console.log('hex sent')
    }catch(err)
    {
        console.error(err);
        return res.status(500).send({ error: 'An error occurred' });
    }
});

app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});



// 实现接收解析单个main.c文件 yes
// 实现交叉编译c文件 yes
// 实现转录ihx文件到hex文件 yes
// 实现回传hex文件 yes
