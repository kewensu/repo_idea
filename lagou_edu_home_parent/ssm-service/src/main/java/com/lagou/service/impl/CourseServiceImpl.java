package com.lagou.service.impl;

import com.lagou.dao.CourseMapper;
import com.lagou.domain.Course;
import com.lagou.domain.CourseVO;
import com.lagou.domain.Teacher;
import com.lagou.service.CourseService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    CourseMapper courseMapper;

    @Override
    public List<Course> findCourseByCondition(CourseVO courseVO) {
        List<Course> courseByCondition = courseMapper.findCourseByCondition(courseVO);
        return courseByCondition;
    }

    @Override
    public void saveCourseAndTeacher(CourseVO courseVO) throws InvocationTargetException, IllegalAccessException {

        // 封装课程信息,使用BeanUtils将信息封装到course中
        Course course = new Course();
        BeanUtils.copyProperties(course, courseVO);

        // 补全课程信息
        Date date = new Date();
        course.setCreateTime(date);
        course.setUpdateTime(date);
        course.setIsDel(0);

        //保存课程
        courseMapper.saveCourse(course);

        // 获取新插入数据的id值
        int id = course.getId();

        // 封装讲师信息
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacher, courseVO);

        // 补全讲师信息
        teacher.setCreateTime(date);
        teacher.setUpdateTime(date);
        teacher.setIsDel(0);
        teacher.setCourseId(id);

        // 保存讲师信息
        courseMapper.saveTeacher(teacher);

    }

    @Override
    public CourseVO findCourseById(Integer id) {
        return courseMapper.findCourseById(id);
    }

    @Override
    public void updateCourseOrTeacher(CourseVO courseVO) throws InvocationTargetException, IllegalAccessException {

        // 封装课程信息,使用BeanUtils将courseVO相关信息封装到course中
        Course course = new Course();
        BeanUtils.copyProperties(course, courseVO);

        // 添加更新时间信息
        Date date = new Date();
        course.setUpdateTime(date);

        //更新课程
        courseMapper.updateCourse(course);

        // 封装讲师信息
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacher, courseVO);

        // 添加讲师信息更新时间及其最新对应的课程id
        // 更新讲师信息时是根据课程id找到对应的教师进行修改
        teacher.setCourseId(course.getId());
        teacher.setUpdateTime(date);

        // 更新讲师信息
        courseMapper.updateTeacher(teacher);

    }

    @Override
    public void updateCourseStatus(int courseid, int status) {
        //1.封装数据
        Course course = new Course();
        course.setId(courseid);
        course.setStatus(status);
        course.setUpdateTime(new Date());

        //2.调用mapper
        courseMapper.updateCourseStatus(course);
    }

}
