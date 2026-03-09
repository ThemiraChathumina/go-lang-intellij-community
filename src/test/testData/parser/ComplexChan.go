package main

// Deeply nested directional channels
type ComplexChan <-chan chan<- <-chan int

// Map containing functions with variadic parameters and multiple returns
type FuncMap map[string]func(...interface{}) (interface{}, error)

// Aliased types (Go 1.9+) vs Defined types
type Alias = map[string]string
type Defined map[string]string

// Struct with blank identifiers and inline anonymous structs
type WeirdStruct struct {
	_ int
	_ string
	Config struct {
		Host string
		Port int
	}
}