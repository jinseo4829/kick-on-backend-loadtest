package kr.kickon.api.domain.country;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.global.common.entities.Country;
import kr.kickon.api.global.common.entities.QCountry;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository countryRepository;

    public Country findByPk(Long pk) {
        BooleanExpression predicate = QCountry.country.pk.eq(pk).and(QCountry.country.status.eq(DataStatus.ACTIVATED));
        Optional<Country> country = countryRepository.findOne(predicate);
        if(country.isPresent()) return country.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_COUNTRY);
    }

    public List<Country> findAll(){
        return countryRepository.findAll();
    }
}
