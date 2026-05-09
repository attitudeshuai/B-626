import request from './request'

export const startGame = (data) => {
  return request.post('/game/start', data)
}

export const makeMove = (data) => {
  return request.post('/game/move', data)
}

export const undoMove = (gameId) => {
  return request.post('/game/undo', { gameId })
}

export const surrender = (gameId) => {
  return request.post('/game/surrender', { gameId })
}

export const saveGame = (data) => {
  return request.post('/game/save', data)
}

export const getHistory = () => {
  return request.get('/game/history')
}

export const getGameDetail = (id) => {
  return request.get(`/game/history/${id}`)
}

export const getWinRateRanking = () => {
  return request.get('/ranking/winrate')
}

export const getWinsRanking = () => {
  return request.get('/ranking/wins')
}
