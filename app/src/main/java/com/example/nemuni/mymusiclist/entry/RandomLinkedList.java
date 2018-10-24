package com.example.nemuni.mymusiclist.entry;

import com.example.nemuni.mymusiclist.bean.MusicMsg;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class RandomLinkedList<E> {
    int size = 0;

    private Node<E> first;

    private Node<E> last;

    private Node<E> curNode;

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    RandomLinkedList(E item) {
        Node<E> node = new Node<>(null, item, null);
        first = last = curNode = node;
    }

    //上一首
    E getPreMusic() {
        if (curNode.prev != null) {
            curNode = curNode.prev;
            return curNode.item;
        } else {
            return null;
        }
    }

    //下一首
    E getNextMusic() {
        if (curNode.next != null) {
            curNode = curNode.next;
            return curNode.item;
        } else {
            return null;
        }
    }

    private void linkFirst(E item) {
        Node<E> f = first;
        Node<E> newNode = new Node<>(null, item, f);
        first = curNode = newNode;
        if (f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }
        size++;
    }

    private void linkLast(E item) {
        Node<E> l = last;
        Node<E> newNode = new Node<>(l, item, null);
        last = curNode = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
    }

    private void linkAfter(E item, Node<E> succ) {
        clearAfter(succ.next);
        Node<E> newNode = new Node<>(succ, item, null);
        last = curNode = newNode;
        succ.next = newNode;
        size++;
    }

    void addFirst(E item) {
        linkFirst(item);
    }

    void addList(E item) {
        if (curNode == null) {
            linkLast(item);
        } else {
            linkAfter(item, curNode);
        }
    }

    void remove(E item) {
        for (Node<E> x = first; x != null;) {
            if (x.item.equals(item)) {
                if (x == curNode) {
                    curNode = x.prev;
                }
                unlink(x);
            }
        }
    }

    private void unlink(Node<E> x) {
        // assert x != null;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
    }

    private void clearAfter(Node<E> from) {
        while (from != null) {
            Node<E> next = from.next;
            from.item = null;
            from.prev = null;
            from.next = null;
            from = next;
            size--;
        }
    }

    public void clear() {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
    }

}
