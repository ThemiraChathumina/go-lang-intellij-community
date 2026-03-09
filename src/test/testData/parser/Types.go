package main

// Basic Struct with tags and embedded fields
type User struct {
	Name string `json:"name" xml:"name"`
	Age  int
	*Address    // Embedded pointer
}

type Address struct {
	City string
}

// Interface with Type Constraints (Generics)
type Number interface {
	~int | ~float64 | float32
}

// Generic Struct
type Box[T Number, U any] struct {
	Item  T
	Label U
}

// Generic Function
func PrintBox[T Number, U any](b Box[T, U]) (T, error) {
	return b.Item, nil
}