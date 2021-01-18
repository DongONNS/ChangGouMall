package com.changgou.user.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name="tb_cities")
public class Cities implements Serializable {

	@Id
	private String cityId;//城市ID

	private String city;//城市名称
	private String provinceId;//省份ID
}
