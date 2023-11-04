const { log } = require("console");

function getMemberList (params) {
  const token=sessionStorage.getItem('token')
  return $axios({
    url: '/employee/page',
    method: 'get',
    params: { ...params,
            token:token}
  })
}

// 修改---启用禁用接口
function enableOrDisableEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params }
  })
}

// 新增---添加员工
function addEmployee (params) {
  const token=sessionStorage.getItem('token')
  console.log(token);
  return $axios({
    url: '/employee',
    method: 'post',
    data: {
            ...params },
     params:{
            token:token
          }
  })
}

// 修改---添加员工
function editEmployee (params) {
  const token=sessionStorage.getItem('token')
  console.log(token);
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params
     },
     params: {
            token: token
          }
  })
 }

// 删除员工
function deleteEmployee (id) {
  return $axios({
    url: `/employee/${id}`,
    method: 'delete'
  })
}

// 修改页面反查详情接口
function queryEmployeeById (id) {
  return $axios({
    url: `/employee/${id}`,
    method: 'get'
  })
}