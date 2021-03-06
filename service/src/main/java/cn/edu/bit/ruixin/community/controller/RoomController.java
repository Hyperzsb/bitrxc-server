package cn.edu.bit.ruixin.community.controller;

import cn.edu.bit.ruixin.base.common.CommonResult;
import cn.edu.bit.ruixin.base.common.ResultCode;
import cn.edu.bit.ruixin.community.domain.Room;
import cn.edu.bit.ruixin.community.domain.Schedule;
import cn.edu.bit.ruixin.community.service.RedisService;
import cn.edu.bit.ruixin.community.service.RoomService;
import cn.edu.bit.ruixin.community.service.ScheduleService;
import cn.edu.bit.ruixin.community.vo.RoomInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * TODO
 *
 * @author 78165
 * @date 2021/1/29
 */
@RestController
@RequestMapping("/room")
@CrossOrigin
public class RoomController {

    @Autowired
    private RoomService roomService;

    private final ReadWriteLock readWriteLock;

    private final Lock readLock;

    @Autowired
    public RoomController(ReadWriteLock readWriteLock) {
        this.readWriteLock = readWriteLock;
        this.readLock = readWriteLock.readLock();
    }

    /**
     * 根据房间ID查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public CommonResult getRoomInfoById(@PathVariable("id") Integer id) {
        Room room = roomService.getRoomInfoById(id);
        return CommonResult.ok(ResultCode.SUCCESS).data("roomInfo", RoomInfoVo.convertToVo(room));
    }

    /**
     * 查询所有房间
     * @return
     */
    @GetMapping("")
    public CommonResult getAllRoomList() {
        List<Room> list = roomService.getAllRoomList();
        List<RoomInfoVo> infoVos = new ArrayList<>();
        for (Room room :
                list) {
            infoVos.add(RoomInfoVo.convertToVo(room));
        }
        return CommonResult.ok(ResultCode.SUCCESS).data("rooms", infoVos);
    }

    /**
     * 分页查询
     * @param current
     * @param limit
     * @return
     */
    @GetMapping("/{current}/{limit}")
    public CommonResult getRoomPages(@PathVariable("current") int current, @PathVariable("limit") int limit) {
        // 构造分页对象
        Pageable pageable = PageRequest.of(current, limit);
        Page<Room> page = roomService.getRoomPages(pageable);
        List<Room> list = page.getContent();
        List<RoomInfoVo> infoVos = new ArrayList<>();
        for (Room room :
                list) {
            infoVos.add(RoomInfoVo.convertToVo(room));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("totalElements", page.getTotalElements());
        map.put("totalPages", page.getTotalPages());
        map.put("hasNext", page.hasNext());
        map.put("hasPrevious", page.hasPrevious());
        map.put("rooms", infoVos);
        return CommonResult.ok(ResultCode.SUCCESS).data(map);
    }

    @GetMapping("/name")
    public CommonResult getRoomByName(@RequestParam("name")String name) {
        Room room = roomService.getRoomInfoByName(name);
        return CommonResult.ok(ResultCode.SUCCESS).data("roomInfo", RoomInfoVo.convertToVo(room));
    }

    @GetMapping("/nameLike")
    public CommonResult getRoomByNameLike(@RequestParam("nameLike")String name) {
        List<Room> list = roomService.getRoomInfoByNameLike("%" + name + "%");
        List<RoomInfoVo> infoVos = new ArrayList<>();
        for (Room room :
                list) {
            infoVos.add(RoomInfoVo.convertToVo(room));
        }
        return CommonResult.ok(ResultCode.SUCCESS).data("rooms", infoVos);
    }

    @GetMapping("/free/time")
    public CommonResult getFreeTimeByRoomId(@RequestParam("roomId") Integer roomId, @RequestParam("username") String username, @RequestParam("date") String date) {
        Map map = roomService.getRoomFreeTime(roomId, username, date);
        return CommonResult.ok(ResultCode.SUCCESS).data(map);
    }
}
