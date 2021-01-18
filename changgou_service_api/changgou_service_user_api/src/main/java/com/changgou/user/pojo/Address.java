package com.changgou.user.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name="tb_address")
public class Address implements Serializable {
	@Id
	private Integer id;//id

	private String username;//用户名
	private String provinceId;//省
	private String cityId;//市
	private String areaId;//县/区
	private String phone;//电话
	private String address;//详细地址
	private String contact;//联系人
	private String isDefault;//是否是默认 1默认 0否
	private String alias;//别名
}
