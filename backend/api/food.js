// 查询列表接口
const getDishPage = (params) => {
  return $axios({
    url: '/dish/page',
    method: 'get',
    params: { ...params,
             token:sessionStorage.getItem('token')   
    }
  })
}

// 删除接口
const deleteDish = (ids) => {
  return $axios({
    url: '/dish',
    method: 'delete',
    params: { ids,
             token:sessionStorage.getItem('token')}
  })
}

// 修改接口
const editDish = (params) => {
  return $axios({
    url: '/dish',
    method: 'put',
    data: { ...params },
    params: {
      token:sessionStorage.getItem('token')
    }
  })
}

// 新增接口
const addDish = (params) => {
  return $axios({
    url: '/dish',
    method: 'post',
    data: { ...params },
    params: {
      token:sessionStorage.getItem('token')
    }
  })
}

// 查询详情
const queryDishById = (id) => {
  return $axios({
    url: `/dish/${id}`,
    method: 'get'
  })
}

// 获取菜品分类列表
const getCategoryList = (params) => {
  return $axios({
    url: '/category/list',
    method: 'get',
    params:{
      ...params,
      token:sessionStorage.getItem('token')
    }
  })
}

// 查菜品列表的接口
const queryDishList = (params) => {
  return $axios({
    url: '/dish/list',
    method: 'get',
    params
  })
}

// 文件down预览
const commonDownload = (params) => {
  return $axios({
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
    },
    url: '/common/download',
    method: 'get',
    params
  })
}

// 起售停售---批量起售停售接口
const dishStatusByStatus = (params) => {
  return $axios({
    url: `/dish/status/${params.status}`,
    method: 'post',
    params: { ids: params.id ,
              token:sessionStorage.getItem('token')}
  })
}