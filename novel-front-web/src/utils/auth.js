const TokenKey = 'Authorization'
const nickNameKey = 'nickName'
const userNameKey = 'userName'
const uidKey = 'uid'

export const getToken = () => {
    return localStorage.getItem(TokenKey);
}

export const setToken = (token) => {
  return localStorage.setItem(TokenKey, token)
}

export const removeToken = () =>  {
  return localStorage.removeItem(TokenKey)
}

export const removeNickName = () =>  {
  return localStorage.removeItem(nickNameKey)
}

export const setNickName = (nickName) => {
  return localStorage.setItem(nickNameKey, nickName)
}

export const getNickName = () => {
  return localStorage.getItem(nickNameKey);
}

export const setUid = (uid) => {
  return localStorage.setItem(uidKey, uid)
}

export const getUid = () => {
  return localStorage.getItem(uidKey);
}

export const removeUid = () =>  {
  return localStorage.removeItem(uidKey)
}

export const setUserName = (userName) => {
  return localStorage.setItem(userNameKey, userName)
}

export const getUserName = () => {
  return localStorage.getItem(userNameKey);
}

export const removeUserName = () =>  {
  return localStorage.removeItem(userNameKey)
}

// 检查是否已登录
export const isLoggedIn = () => {
  return !!getToken()
}

// 清除所有认证信息
export const clearAuth = () => {
  removeToken()
  removeNickName()
  removeUserName()
  removeUid()
}
