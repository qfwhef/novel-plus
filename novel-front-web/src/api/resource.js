import request from '../utils/request'

export function getImgVerifyCode() {
    return request.get('/front/resource/img_verify_code');
}

export function uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/front/resource/image', formData);
}
