package com.ride2go.ridetogtfsconverter.model.item.routing.osm;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;

@Getter
public class Node {

	@XmlAttribute
	String lat;

	@XmlAttribute
	String lon;
}
