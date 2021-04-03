package cn.edu.bit.ruixin.community.service.impl;

import cn.edu.bit.ruixin.community.domain.Appointment;
import cn.edu.bit.ruixin.community.domain.Schedule;
import cn.edu.bit.ruixin.community.exception.AppointmentDaoException;
import cn.edu.bit.ruixin.community.myenum.AppointmentStatus;
import cn.edu.bit.ruixin.community.repository.AppointmentRepository;
import cn.edu.bit.ruixin.community.repository.ScheduleRepository;
import cn.edu.bit.ruixin.community.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TODO
 *
 * @author 78165
 * @date 2021/2/6
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Appointment> getAllAppointmentByUsername(String username) {
        // 根据用户wxid查询所有记录并根据发起时间排序
        return appointmentRepository.findAllByLauncherEqualsOrderByLaunchDate(username);
    }

    @Transactional(readOnly = true)
    @Override
    public Appointment getAppointmentById(Integer id) {
        Appointment appointment = appointmentRepository.findAppointmentById(id);
        System.out.println(appointment);
        return appointment;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @Override
    public void addANewAppointment(Appointment appointment) {
        // 判断该房间所预约时间段是否空闲，比较房间ID，预约的日期，预约的时间段，不能对含有预约记录状态为receive、Signed
        Integer roomId = appointment.getRoomId();
        Integer launchTime = appointment.getLaunchTime();
        Date execDate = appointment.getExecDate();
        Appointment getAppointment = appointmentRepository.findReceivedAppointment(roomId, execDate, launchTime, AppointmentStatus.RECEIVE.getStatus(), AppointmentStatus.SIGNED.getStatus());
        if (getAppointment != null) {
            throw new AppointmentDaoException("该房间此时间段已被占用!");
        } else {
            String launcher = appointment.getLauncher();
            getAppointment = appointmentRepository.findAppointmentByLauncherEqualsAndRoomIdEqualsAndExecDateEqualsAndLaunchTimeEqualsAndStatusEquals(launcher, roomId, execDate, launchTime, AppointmentStatus.NEW.getStatus());
            if (getAppointment != null) {
                throw new AppointmentDaoException("你已申请预约该房间的此时间段，请等待审批！");
            } else {
                appointment.setStatus(AppointmentStatus.NEW.getStatus());
                Date launchDate = new Date();
                // 还应该保证预约发起时间早于要预约的时间段的起始
                Schedule schedule = scheduleRepository.getOne(launchTime);
                String begin = schedule.getBegin();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateFormat.format(execDate);
                String dateTime = date + " " + begin;
                Date executeDate = null;
                try {
                    executeDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateTime);
                } catch (ParseException e) {
                    throw new AppointmentDaoException("实际执行预约使用的时间格式有误！");
                }
                if (launchDate.before(executeDate)) {
                    appointment.setLaunchDate(launchDate);
                    appointmentRepository.save(appointment);
                } else {
                    throw new AppointmentDaoException("该时间段已过，不可预约！");
                }
            }
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @Override
    public void cancelAppointmentById(Integer id) {
        Appointment appointment = appointmentRepository.findAppointmentById(id);
        if (appointment != null) {
            if (appointment.getStatus().equals("receive")) {
                throw new AppointmentDaoException("该预约已审批通过，不可撤销!");
            }
            if (appointment.getStatus().equals("reject")) {
                throw new AppointmentDaoException("该预约已被审批驳回，不可撤销!");
            }
            if (appointment.getStatus().equals("cancel")) {
                throw new AppointmentDaoException("该预约已被撤销，不可重复撤销!");
            }
            if (appointment.getStatus().equals(AppointmentStatus.SIGNED.getStatus())) {
                throw new AppointmentDaoException("该预约已被签到，不可重复撤销!");
            }
            if (appointment.getStatus().equals(AppointmentStatus.ILLEGAL.getStatus()) || appointment.getStatus().equals(AppointmentStatus.FINISH.getStatus()) || appointment.getStatus().equals(AppointmentStatus.MISSED.getStatus())) {
                throw new AppointmentDaoException("该预约已不可撤销!");
            }
            // 撤销预约
            appointment.setStatus("cancel");
            appointmentRepository.save(appointment);
        } else {
            throw new AppointmentDaoException("不存在该预约记录!");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Appointment> getAllAppointment(String status) {
        if (status != null && !status.equals("")) {
            return appointmentRepository.findAllByStatusEqualsOrderByExecDateAscLaunchTimeAscLaunchDateAsc(status);
        } else {
            return appointmentRepository.findAll();
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public void checkOutAppointment(Integer id, String status, String conductor, String checkNote) {
        Appointment appointment = appointmentRepository.findAppointmentById(id);
        if (appointment != null) {
            if (appointment.getStatus().equals(AppointmentStatus.CANCEL.getStatus())) {
                throw new AppointmentDaoException("该预约已经取消，无法审批!");
            }
            if (appointment.getStatus().equals(AppointmentStatus.RECEIVE.getStatus())) {
                if (!AppointmentStatus.SIGNED.getStatus().equals(status) && !AppointmentStatus.MISSED.getStatus().equals(status)) {
                    throw new AppointmentDaoException("该预约已审批通过，不可执行该操作!");
                }
            }
            if (appointment.getStatus().equals(AppointmentStatus.REJECT.getStatus())) {
                throw new AppointmentDaoException("该预约已审批驳回，不可重复操作!");
            }
            if (appointment.getStatus().equals(AppointmentStatus.MISSED.getStatus())) {
                if (!AppointmentStatus.SIGNED.getStatus().equals(status)) {
                    throw new AppointmentDaoException("该预约已爽约，只能重新签到!");
                }
            }
            if (appointment.getStatus().equals(AppointmentStatus.ILLEGAL.getStatus())) {
                if (!AppointmentStatus.FINISH.getStatus().equals(status)) {
                    throw new AppointmentDaoException("该预约未签退，只能修改为签退！");
                }
            }
            if (conductor != null && !conductor.equals("")) {
                appointment.setStatus(status);
                appointment.setConductor(conductor);
                appointment.setCheckNote(checkNote);
                appointmentRepository.save(appointment);
            } else {
                throw new AppointmentDaoException("审批人姓名不可为空!");
            }
        }
    }

    @Override
    public Page<Appointment> getAppointmentPages(Pageable pageable, String status) {
        if (status != null && !"".equals(status)) {
            Appointment appointment = new Appointment();
            appointment.setStatus(status);
            Example<Appointment> example = Example.of(appointment);
            return appointmentRepository.findAll(example, pageable);
        } else {
            return appointmentRepository.findAll(pageable);
        }
    }
}
