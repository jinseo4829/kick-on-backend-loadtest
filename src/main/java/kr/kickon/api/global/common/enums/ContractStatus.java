package kr.kickon.api.global.common.enums;

public enum ContractStatus {
  BEFORE_CONTACT,
  CONTACTING,
  CONTACT_COMPLETE,
  REJECTED,
  BEFORE_CONTRACT, //계약 전
  CONTRACT_IN_PROGRESS, //계약 진행 중
  CONTRACT_EXPIRED, //계약 만료
  CONTRACT_TERMINATED //중도 해지
}
