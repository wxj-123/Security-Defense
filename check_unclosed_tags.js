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

// 检查未闭合的HTML标签
function checkUnclosedTags(content, fileName) {
  // 提取template部分
  const templateMatch = content.match(/<template[^>]*>([\s\S]*)<\/template>/);
  if (!templateMatch) return true;
  
  const templateContent = templateMatch[1];
  
  // 移除注释
  const contentWithoutComments = templateContent.replace(/<!--[\s\S]*?-->/g, '');
  
  const tagStack = [];
  // 更精确的标签匹配正则表达式
  const tagRegex = /<\/?([a-zA-Z][a-zA-Z0-9-]*)[^>]*>/g;
  let match;
  
  while ((match = tagRegex.exec(contentWithoutComments)) !== null) {
    const fullTag = match[0];
    const tagName = match[1];
    const isClosing = fullTag.startsWith('</');
    
    // 忽略自闭合标签和特殊标签
    if (fullTag.endsWith('/>') || 
        tagName === 'img' || 
        tagName === 'input' || 
        tagName === 'br' || 
        tagName === 'hr' || 
        tagName === 'meta' || 
        tagName === 'link' ||
        tagName === 'el-badge') { // el-badge 是自闭合的
      continue;
    }
    
    if (isClosing) {
      if (tagStack.length === 0 || tagStack[tagStack.length - 1] !== tagName) {
        console.log(`在文件 ${fileName} 中发现未匹配的闭合标签: </${tagName}> (行号附近: ${getLineNumber(contentWithoutComments, match.index)})`);
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

// 获取行号
function getLineNumber(content, index) {
  return content.substr(0, index).split('\n').length;
}

// 主函数
function main() {
  const vueFiles = findVueFiles('./frontend/src');
  let hasError = false;
  
  for (const file of vueFiles) {
    try {
      const content = fs.readFileSync(file, 'utf8');
      if (!checkUnclosedTags(content, file)) {
        hasError = true;
      }
    } catch (err) {
      console.log(`读取文件 ${file} 时出错: ${err.message}`);
    }
  }
  
  if (!hasError) {
    console.log('所有Vue文件中的HTML标签都正确闭合。');
  }
}

main();