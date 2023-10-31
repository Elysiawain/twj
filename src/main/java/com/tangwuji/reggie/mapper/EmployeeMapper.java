package com.tangwuji.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangwuji.reggie.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

//员工mapper接口
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee>{//这里继承Mybatis-Plus提供的BaseMapper
    //根据用户名查询数据
    Employee selectByName(String username);
    //按照用户名查找用户
    Employee selectByUsername(String username);

    /**
     * 增加员工
     * @param
     */
    void insertEmp(long id,String name, String username, String password, String phone, String sex, String idNumber,  LocalDateTime createTime, LocalDateTime updateTime, Long createUser, Long updateUser);

    @Select("select * from employee")
    List<Object> pageHelperLimit(String name);
}
