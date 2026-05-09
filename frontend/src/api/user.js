import request from './request'

export const getCurrentUser = () => {
  return request.get('/users/me')
}

export const updateUser = (data) => {
  return request.put('/users/me', data)
}
