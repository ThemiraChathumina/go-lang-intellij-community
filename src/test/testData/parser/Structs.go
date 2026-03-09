package main

type Point struct {
    X, Y int
}

type Node struct {
    Data int
    Next *Node
}

func structs() {
    p := Point{X: 1, Y: 2}
    n := &Node{Data: 10, Next: nil}
}