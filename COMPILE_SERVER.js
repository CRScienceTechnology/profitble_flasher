const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const app = express();
const PORT = process.env.PORT || 11451;

app.use(bodyParser.json());

app.post('/COMPILE_SERVER', async (req, res) => {
    try {
        const sourceCode = req.body.main; // 假设请求体中的C源代码字段名为sourceCode

        if (!sourceCode) {
            return res.status(400).send({ error: 'Source code not provided' });
        }

        // 将源代码写入临时文件
        const tempFilePath = path.join(__dirname, 'temp', 'main.c');
        fs.writeFileSync(tempFilePath, sourceCode, 'utf8');

        // 使用SDCC编译C源代码
        exec(`python sdcc ${tempFilePath}`, (error, stdout, stderr) => {
            if (error) {
                console.error(`执行出错: ${error.message}`);
                return res.status(500).send({ error: 'Compilation failed' });
            }
            if (stderr) {
                console.error(`stderr: ${stderr}`);
                return res.status(500).send({ error: 'Compilation failed', stderr });
            }
            console.log(`stdout: ${stdout}`);
            return res.status(200).send({ message: 'Compilation successful', stdout });
        });
    } catch (err) {
        console.error(err);
        return res.status(500).send({ error: 'An error occurred' });
    }
});

app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});


// 实现接收解析单个main.c文件
// 实现交叉编译c文件
// 实现转录ihx文件到hex文件
// 实现回传hex文件