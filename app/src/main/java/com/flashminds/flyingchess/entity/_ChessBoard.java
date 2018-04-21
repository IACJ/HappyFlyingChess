package com.flashminds.flyingchess.entity;

/**
 * Created by karthur on 2016/4/9.
 *
 * Edited by IACJ on 2018/4/9
 */
@Deprecated
public class _ChessBoard {//chess board data
    public static final int COLOR_RED = 0, COLOR_GREEN = 1, COLOR_BLUE = 2, COLOR_YELLOW = 3;//玩家颜色
    public static final int COLOR_Z = -1, COLOR_X = -2;
    private boolean overflow;
    private Dice dice;
    private Airplane[] airplanes;
    public int[][][] map = {
            // 红色
            {{5, 18}, // 停机坪
                    {6, 16}, {5, 15},
                    {5, 14}, {6, 13}, {5, 12}, {4, 13},
                    {3, 13}, {2, 12}, {1, 11}, {1, 10},
                    {1, 9}, {1, 8}, {1, 7}, {2, 6},
                    {3, 5}, {4, 5}, {5, 6}, {6, 5},
                    {5, 4}, {5, 3}, {6, 2}, {7, 1},
                    {8, 1}, {9, 1}, {10, 1}, {11, 1},
                    {12, 2}, {13, 3}, {13, 4}, {12, 5},
                    {13, 6}, {14, 5}, {15, 5}, {16, 6},
                    {17, 7}, {17, 8}, {17, 9}, {17, 10},
                    {17, 11}, {16, 12}, {15, 13}, {14, 13},
                    {13, 12}, {12, 13}, {13, 14}, {13, 15},
                    {12, 16}, {11, 17}, {10, 17}, {9, 16},
                    {9, 15}, {9, 14}, {9, 13}, {9, 12}, {9, 11}, {9, 10}}, // 最后6格
            // 绿色
            {{18, 13},// 停机坪
                    {16, 12}, {15, 13},
                    {14, 13}, {13, 12}, {12, 13}, {13, 14},
                    {13, 15}, {12, 16}, {11, 17}, {10, 17},
                    {9, 17}, {8, 17}, {7, 17}, {6, 16},
                    {5, 15}, {5, 14}, {6, 13}, {5, 12},
                    {4, 13}, {3, 13}, {2, 12}, {1, 11},
                    {1, 10}, {1, 9}, {1, 8}, {1, 7},
                    {2, 6}, {3, 5}, {4, 5}, {5, 6},
                    {6, 5}, {5, 4}, {5, 3}, {6, 2},
                    {7, 1}, {8, 1}, {9, 1}, {10, 1},
                    {11, 1}, {12, 2}, {13, 3}, {13, 4},
                    {12, 5}, {13, 6}, {14, 5}, {15, 5},
                    {16, 6}, {17, 7}, {17, 8}, {16, 9},
                    {15, 9}, {14, 9}, {13, 9}, {12, 9}, {11, 9}, {10, 9}}, //最后六格
            // 蓝色
            {{13, 0},// 停机坪
                    {12, 2}, {13, 3},
                    {13, 4}, {12, 5}, {13, 6}, {14, 5},
                    {15, 5}, {16, 6}, {17, 7}, {17, 8},
                    {17, 9}, {17, 10}, {17, 11}, {16, 12},
                    {15, 13}, {14, 13}, {13, 12}, {12, 13},
                    {13, 14}, {13, 15}, {12, 16}, {11, 17},
                    {10, 17}, {9, 17}, {8, 17}, {7, 17},
                    {6, 16}, {5, 15}, {5, 14}, {6, 13},
                    {5, 12}, {4, 13}, {3, 13}, {2, 12},
                    {1, 11}, {1, 10}, {1, 9}, {1, 8},
                    {1, 7}, {2, 6}, {3, 5}, {4, 5},
                    {5, 6}, {6, 5}, {5, 4}, {5, 3},
                    {6, 2}, {7, 1}, {8, 1}, {9, 2},
                    {9, 3}, {9, 4}, {9, 5}, {9, 6}, {9, 7}, {9, 8}}, // 最后六格
            // 黄色
            {{0, 5},// 停机坪
                    {2, 6}, {3, 5},
                    {4, 5}, {5, 6}, {6, 5}, {5, 4},
                    {5, 3}, {6, 2}, {7, 1}, {8, 1},
                    {9, 1}, {10, 1}, {11, 1}, {12, 2},
                    {13, 3}, {13, 4}, {12, 5}, {13, 6},
                    {14, 5}, {15, 5}, {16, 6}, {17, 7},
                    {17, 8}, {17, 9}, {17, 10}, {17, 11},
                    {16, 12}, {15, 13}, {14, 13}, {13, 12},
                    {12, 13}, {13, 14}, {13, 15}, {12, 16},
                    {11, 17}, {10, 17}, {9, 17}, {8, 17},
                    {7, 17}, {6, 16}, {5, 15}, {5, 14},
                    {6, 13}, {5, 12}, {4, 13}, {3, 13},
                    {2, 12}, {1, 11}, {1, 10}, {2, 9},
                    {3, 9}, {4, 9}, {5, 9}, {6, 9}, {7, 9}, {8, 9}} // 最后六格
    };
    public int[][][] mapStart = {
            {{1, 15}, {3, 15}, {1, 17}, {3, 17}},
            {{15, 15}, {17, 15}, {15, 17}, {17, 17}},
            {{15, 1}, {17, 1}, {15, 3}, {17, 3}},
            {{1, 1}, {3, 1}, {1, 3}, {3, 3}}
    };

    public void init() {//call in game manager when a new game start
        dice = new Dice();
        airplanes = new Airplane[4];
        airplanes[0] = new Airplane();
        airplanes[1] = new Airplane();
        airplanes[2] = new Airplane();
        airplanes[3] = new Airplane();
    }

    public Dice getDice() {//call in game manager when user throw a dice
        return dice;
    }

    public Airplane getAirplane(int color) {
        return airplanes[color];
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

}
