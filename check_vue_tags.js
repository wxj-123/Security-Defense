const fs = require('fs');
const path = require('path');

// 递归查找所有Vue文件
function findVueFiles(dir, files = []) {
  const entries = fs.readdirSync(dir);
  for (const entry of entries) {
    const entryPath = path.join(dir, entry);
    const stat = fs.statSync(entryPath);
    if (stat.isDirectory()) {
      findVueFiles(entryPath, files);
    } else if (entry.endsWith('.vue')) {
      files.push(entryPath);
    }
  }
  return files;
}

// 检查标签是否闭合
function checkTags(content, fileName) {
  const tagStack = [];
  const tagRegex = /<\/?([a-zA-Z][a-zA-Z0-9-]*)[^>]*>/g;
  let match;
  
  while ((match = tagRegex.exec(content)) !== null) {
    const fullTag = match[0];
    const tagName = match[1];
    const isClosing = fullTag.startsWith('</');
    
    // 忽略自闭合标签
    if (fullTag.endsWith('/>')) {
      continue;
    }
    
    if (isClosing) {
      if (tagStack.length === 0 || tagStack[tagStack.length - 1] !== tagName) {
        console.log(`在文件 ${fileName} 中发现未匹配的闭合标签: </${tagName}>`);
        return false;
      }
      tagStack.pop();
    } else {
      tagStack.push(tagName);
    }
  }
  
  if (tagStack.length > 0) {
    console.log(`在文件 ${fileName} 中发现未闭合的标签: <${tagStack[tagStack.length - 1]}>`);
    return false;
  }
  
  return true;
}

// 主函数
function main() {
  const vueFiles = findVueFiles('./frontend/src');
  let hasError = false;
  
  for (const file of vueFiles) {
    try {
      const content = fs.readFileSync(file, 'utf8');
      if (!checkTags(content, file)) {
        hasError = true;
      }
    } catch (err) {
      console.log(`读取文件 ${file} 时出错: ${err.message}`);
    }
  }
  
  if (!hasError) {
    console.log('所有Vue文件中的标签都正确闭合。');
  }
}

main();