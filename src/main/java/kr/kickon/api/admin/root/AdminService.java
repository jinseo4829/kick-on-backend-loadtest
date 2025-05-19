package kr.kickon.api.admin.root;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.Admin;
import kr.kickon.api.global.common.entities.QAdmin;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService implements BaseService<Admin> {
    private final AdminRepository adminRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Admin findById(String uuid) {
        BooleanExpression predicate = QAdmin.admin.id.eq(uuid).and(QAdmin.admin.status.eq(DataStatus.ACTIVATED));
        Optional<Admin> admin = adminRepository.findOne(predicate);
        return admin.orElse(null);
    }

    @Override
    public Admin findByPk(Long pk) {
        BooleanExpression predicate = QAdmin.admin.pk.eq(pk).and(QAdmin.admin.status.eq(DataStatus.ACTIVATED));
        Optional<Admin> admin = adminRepository.findOne(predicate);
        return admin.orElse(null);
    }

    public Admin findByEmail(String email) {
        return adminRepository.findByEmailAndStatus(email, DataStatus.ACTIVATED);
    }

    public Admin createAdmin(String email, String plainPassword) {
        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(plainPassword);

        // Admin 객체 생성
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(encryptedPassword);
        return adminRepository.save(admin);
    }

    public boolean checkPassword(Admin admin, String plainPassword) {
        // 비밀번호가 맞는지 확인
        return passwordEncoder.matches(plainPassword, admin.getPassword());
    }
}
