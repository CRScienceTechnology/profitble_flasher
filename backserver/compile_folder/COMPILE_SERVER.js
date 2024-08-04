const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(bodyParser.json());







app.post('/COMPILE_SERVER', async (req, res) => {
    try {
               // 解析http请求体



               // 执行源码编译
        
               var source_code_folder='/root/REMOTE_COMPILER/compile_folder/source_code_folder'    // 源代码存储路径
               var output_folder='/root/REMOTE_COMPILER/compile_folder/output_folder'         // 编译输出路径
               var codename='main'    // hex文件名


               // 将源代码生成ihx文件 相当于在终端执行sdcc '/root.../main.c' -o '/root...foleder/'

               exec(`sdcc '${source_code_folder}/${codename}.c' -o '${output_folder}/' `, (error) => {
                if (error) 
                {
                   console.error(`执行出错: ${error.message}`);
                   return res.status(500).send({ error: 'Compilation failed' });
                }
                else
                {   
                
                    // sdcc命令直接输出的是ihx文件，此处调用packihx将文件转换为hex
                    // 相当于在终端执行packihx '/root.../main.ihx' > '/root../main.hex'

                    exec(`packihx '${output_folder}/${codename}.ihx' > '${output_folder}/${codename}.hex'`,(err)=>
                    {   
                        fs.access(`'${output_folder}/${codename}.hex'`,fs.constants.F_OK,(err) =>{if(err){console.log("未生成hex文件");}}) // 判断对象文件是否生成
                    })
               }});

               // 回传hex文件



        const sourceCode = req.body.main; // 假设请求体中的C源代码字段名为sourceCode

        if (!sourceCode) {
            return res.status(400).send({ error: 'Source code not provided' });
        }


    } catch (err) {
        console.error(err);
        return res.status(500).send({ error: 'An error occurred' });
    }
});

app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});


// 实现接收解析单个main.c文件 
// 实现交叉编译c文件 yes
// 实现转录ihx文件到hex文件 yes
// 实现回传hex文件 