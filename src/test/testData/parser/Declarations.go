package main

import (
	"fmt"
	_ "os"       // Blank import
	. "math"     // Dot import
	alias "time" // Aliased import
)

const (
	A = iota
	B
	C string = "test"
)

var (
	x, y int = 1, 2
	z    = 3
	w    int
)

func Variables() {
	a, b := 1, 2
	a, b = b, a // Tuple assignment
	c := a + b
	_ = c
}