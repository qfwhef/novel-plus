/**
 * 图片URL处理工具函数
 */

/**
 * 智能处理图片URL，兼容新旧两种格式
 * @param {string} imageUrl - 图片URL或路径
 * @param {string} fallback - 默认图片
 * @returns {string} 处理后的完整图片URL
 */
export function getImageUrl(imageUrl, fallback = '') {
  if (!imageUrl) return fallback;
  
  // 如果是完整的HTTP/HTTPS URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  
  // 如果是相对路径，拼接基础URL
  const baseUrl = process.env.VUE_APP_BASE_IMG_URL;
  if (imageUrl.startsWith('/')) {
    return baseUrl + imageUrl;
  }
  
  // 其他情况也拼接基础URL
  return baseUrl + '/' + imageUrl;
}

/**
 * 检查图片URL是否为完整URL
 * @param {string} url - 图片URL
 * @returns {boolean} 是否为完整URL
 */
export function isFullUrl(url) {
  return url && (url.startsWith('http://') || url.startsWith('https://'));
}

/**
 * 获取图片文件名
 * @param {string} url - 图片URL
 * @returns {string} 文件名
 */
export function getImageFileName(url) {
  if (!url) return '';
  
  const parts = url.split('/');
  return parts[parts.length - 1];
}