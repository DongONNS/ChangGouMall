package com.changgou.user.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name="tb_areas")
public class Areas implements Serializable {

	@Id
	private String areaId;//区域ID

	private String area;//区域名称
	private String cityId;//城市ID
}
