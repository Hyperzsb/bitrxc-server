package cn.edu.bit.ruixin.community.vo;

import cn.edu.bit.ruixin.community.annotation.FieldNeedCheck;
import cn.edu.bit.ruixin.community.domain.User;

/**
 * TODO
 *
 * @author 78165
 * @date 2021/2/5
 */
public class UserInfoVo {

    // wxid
    private String username;

    // 标记注解，需要敏感词过滤
    @FieldNeedCheck
    private String name;

    private String phone;

    private String password;

    private String schoolId;

    @FieldNeedCheck
    private String organization;

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public static User convertToPo(UserInfoVo infoVo) {
        User user = new User();
        user.setUsername(infoVo.getUsername());
        user.setName(infoVo.getName());
        user.setPhone(infoVo.getPhone());
        user.setOrganization(infoVo.getOrganization());
        user.setPassword(infoVo.getPassword());
        user.setSchoolId(infoVo.schoolId);
        return user;
    }

    public static UserInfoVo convertToVo(User user) {
        UserInfoVo infoVo = new UserInfoVo();
        infoVo.setName(user.getName());
        infoVo.setOrganization(user.getOrganization());
        infoVo.setPhone(user.getPhone());
        infoVo.setUsername(user.getUsername());
        infoVo.setSchoolId(user.getSchoolId());
        return infoVo;
    }

    @Override
    public String toString() {
        return "UserInfoVo{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", schoolId='" + schoolId + '\'' +
                ", organization='" + organization + '\'' +
                '}';
    }
}
