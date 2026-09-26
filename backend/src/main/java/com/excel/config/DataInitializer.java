package com.excel.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.excel.entity.User;
import com.excel.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
    }

    private void initAdminUser() {
        // 检查admin用户是否存在
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")
        );

        if (existingUser == null) {
            // 创建管理员用户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@example.com");
            admin.setPhone("13800000000");
            admin.setStatus(1);
            admin.setDeleted(0);
            userMapper.insert(admin);
            logger.info("初始化管理员用户成功: admin / admin123");
        } else {
            // 更新密码确保一致
            String encodedPassword = passwordEncoder.encode("admin123");
            existingUser.setPassword(encodedPassword);
            userMapper.updateById(existingUser);
            logger.info("更新管理员密码成功");
        }
    }
}
