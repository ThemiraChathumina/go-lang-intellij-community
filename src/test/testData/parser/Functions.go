package main

func add(a int, b int) int {
    return a + b
}

func multiReturn(s string) (string, int) {
    return s, len(s)
}

type T struct{}

func (t *T) Method() {
}

func main() {
    add(1, 2)
}