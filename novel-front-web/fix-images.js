// 批量修复图片显示问题的脚本
const fs = require('fs');
const path = require('path');

// 需要修复的文件列表
const filesToFix = [
  'src/components/home/BookUpdateRank.vue',
  'src/components/home/BookNewestRank.vue',
  'src/views/Book.vue',
  'src/views/UserComment.vue'
];

// 修复函数
function fixImageUrls(filePath) {
  console.log(`正在修复: ${filePath}`);
  
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    
    // 1. 添加import语句
    if (!content.includes('import { getImageUrl } from "@/utils/imageHelper"')) {
      // 找到最后一个import语句的位置
      const importRegex = /import .* from .*;\n/g;
      let lastImportMatch;
      let match;
      while ((match = importRegex.exec(content)) !== null) {
        lastImportMatch = match;
      }
      
      if (lastImportMatch) {
        const insertPos = lastImportMatch.index + lastImportMatch[0].length;
        content = content.slice(0, insertPos) + 
                 'import { getImageUrl } from "@/utils/imageHelper";\n' + 
                 content.slice(insertPos);
      }
    }
    
    // 2. 替换图片URL模式
    content = content.replace(
      /\$\{imgBaseUrl\}\` \+ \`\$\{item\.picUrl\}/g,
      'getBookImageUrl(item.picUrl)'
    );
    
    content = content.replace(
      /imgBaseUrl \+ item\.commentBookPic/g,
      'getBookImageUrl(item.commentBookPic)'
    );
    
    content = content.replace(
      /imgBaseUrl \+ item\.commentUserPhoto/g,
      'getBookImageUrl(item.commentUserPhoto)'
    );
    
    content = content.replace(
      /\$\{imgBaseUrl\}\` \+ \`\$\{book\.picUrl\}/g,
      'getBookImageUrl(book.picUrl)'
    );
    
    // 3. 在return语句中添加getBookImageUrl函数
    if (!content.includes('getBookImageUrl,')) {
      content = content.replace(
        /(return\s*{[^}]*)(}\s*;)/,
        '$1  getBookImageUrl,\n    $2'
      );
      
      // 添加函数定义
      const functionDef = `
    // 智能处理图片URL
    const getBookImageUrl = (imageUrl) => {
      return getImageUrl(imageUrl, '/assets/images/default.gif');
    };
`;
      
      content = content.replace(
        /(return\s*{)/,
        functionDef + '\n    $1'
      );
    }
    
    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`✅ 修复完成: ${filePath}`);
    
  } catch (error) {
    console.error(`❌ 修复失败: ${filePath}`, error.message);
  }
}

// 执行修复
filesToFix.forEach(fixImageUrls);

console.log('所有文件修复完成！');