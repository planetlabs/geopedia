package com.sinergise.geopedia.rendering;

import com.sinergise.geopedia.style.SymLoader;

public class SymDraw
{
	public static void draw(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		int flags;
		if (sym == null || (flags = sym[0]) == 0)
			return;
		if ((flags & SymLoader.HAS_C2) != 0) {
			if ((flags & SymLoader.HAS_C1) != 0) {
				if ((c2 >>> 24) == 0) {
					if ((c1 >>> 24) == 0) {
						if ((flags & SymLoader.HAS_OVER) != 0) {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw243(img, w, h, sym, x, y);
							} else {
								draw242(img, w, h, sym, x, y);
							}
						}
					} else {
						if ((c1 >>> 24) == 255) {
							if ((flags & SymLoader.HAS_OVER) != 0) {
								if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
									draw231(img, w, h, sym, x, y, c1);
								} else {
									draw230(img, w, h, sym, x, y, c1);
								}
							} else {
								if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
									draw229(img, w, h, sym, x, y, c1);
								} else {
									draw228(img, w, h, sym, x, y, c1);
								}
							}
						} else {
							if ((flags & SymLoader.HAS_OVER) != 0) {
								if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
									draw227(img, w, h, sym, x, y, c1);
								} else {
									draw226(img, w, h, sym, x, y, c1);
								}
							} else {
								if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
									draw225(img, w, h, sym, x, y, c1);
								} else {
									draw224(img, w, h, sym, x, y, c1);
								}
							}
						}
					}
				} else {
					if ((c1 >>> 24) == 0) {
						if ((c2 >>> 24) == 255) {
							if ((flags & SymLoader.HAS_OVER) != 0) {
								if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
									draw219(img, w, h, sym, x, y, c2);
								} else {
									draw218(img, w, h, sym, x, y, c2);
								}
							} else {
								if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
									draw217(img, w, h, sym, x, y, c2);
								} else {
									draw216(img, w, h, sym, x, y, c2);
								}
							}
						} else {
							if (!((c1 >>> 24) == 255)) {
								if ((flags & SymLoader.HAS_OVER) != 0) {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw211(img, w, h, sym, x, y, c2);
									} else {
										draw210(img, w, h, sym, x, y, c2);
									}
								} else {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw209(img, w, h, sym, x, y, c2);
									} else {
										draw208(img, w, h, sym, x, y, c2);
									}
								}
							}
						}
					} else {
						if ((c2 >>> 24) == 255) {
							if ((c1 >>> 24) == 255) {
								if ((flags & SymLoader.HAS_OVER) != 0) {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw207(img, w, h, sym, x, y, c1, c2);
									} else {
										draw206(img, w, h, sym, x, y, c1, c2);
									}
								} else {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw205(img, w, h, sym, x, y, c1, c2);
									} else {
										draw204(img, w, h, sym, x, y, c1, c2);
									}
								}
							} else {
								if ((flags & SymLoader.HAS_OVER) != 0) {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw203(img, w, h, sym, x, y, c1, c2);
									} else {
										draw202(img, w, h, sym, x, y, c1, c2);
									}
								} else {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw201(img, w, h, sym, x, y, c1, c2);
									} else {
										draw200(img, w, h, sym, x, y, c1, c2);
									}
								}
							}
						} else {
							if ((c1 >>> 24) == 255) {
								if ((flags & SymLoader.HAS_OVER) != 0) {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw199(img, w, h, sym, x, y, c1, c2);
									} else {
										draw198(img, w, h, sym, x, y, c1, c2);
									}
								} else {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw197(img, w, h, sym, x, y, c1, c2);
									} else {
										draw196(img, w, h, sym, x, y, c1, c2);
									}
								}
							} else {
								if ((flags & SymLoader.HAS_OVER) != 0) {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw195(img, w, h, sym, x, y, c1, c2);
									} else {
										draw194(img, w, h, sym, x, y, c1, c2);
									}
								} else {
									if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0
									                && y + sym[4] < h) {
										draw193(img, w, h, sym, x, y, c1, c2);
									} else {
										draw192(img, w, h, sym, x, y, c1, c2);
									}
								}
							}
						}
					}
				}
			} else {
				if ((c2 >>> 24) == 0) {
					if ((flags & SymLoader.HAS_OVER) != 0) {
						if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
							draw179(img, w, h, sym, x, y);
						} else {
							draw178(img, w, h, sym, x, y);
						}
					}
				} else {
					if ((c2 >>> 24) == 255) {
						if ((flags & SymLoader.HAS_OVER) != 0) {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw155(img, w, h, sym, x, y, c2);
							} else {
								draw154(img, w, h, sym, x, y, c2);
							}
						} else {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw153(img, w, h, sym, x, y, c2);
							} else {
								draw152(img, w, h, sym, x, y, c2);
							}
						}
					} else {
						if ((flags & SymLoader.HAS_OVER) != 0) {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw147(img, w, h, sym, x, y, c2);
							} else {
								draw146(img, w, h, sym, x, y, c2);
							}
						} else {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw145(img, w, h, sym, x, y, c2);
							} else {
								draw144(img, w, h, sym, x, y, c2);
							}
						}
					}
				}
			}
		} else {
			if ((flags & SymLoader.HAS_C1) != 0) {
				if ((c1 >>> 24) == 0) {
					if ((flags & SymLoader.HAS_OVER) != 0) {
						if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
							draw115(img, w, h, sym, x, y);
						} else {
							draw114(img, w, h, sym, x, y);
						}
					}
				} else {
					if ((c1 >>> 24) == 255) {
						if ((flags & SymLoader.HAS_OVER) != 0) {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw103(img, w, h, sym, x, y, c1);
							} else {
								draw102(img, w, h, sym, x, y, c1);
							}
						} else {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw101(img, w, h, sym, x, y, c1);
							} else {
								draw100(img, w, h, sym, x, y, c1);
							}
						}
					} else {
						if ((flags & SymLoader.HAS_OVER) != 0) {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw99(img, w, h, sym, x, y, c1);
							} else {
								draw98(img, w, h, sym, x, y, c1);
							}
						} else {
							if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
								draw97(img, w, h, sym, x, y, c1);
							} else {
								draw96(img, w, h, sym, x, y, c1);
							}
						}
					}
				}
			} else {
				if ((flags & SymLoader.HAS_OVER) != 0) {
					if (x + sym[1] >= 0 && x + sym[3] < w && y + sym[2] >= 0 && y + sym[4] < h) {
						draw51(img, w, h, sym, x, y);
					} else {
						draw50(img, w, h, sym, x, y);
					}
				}
			}
		}
	}

	static void draw50(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasOver
		// emptyC1
		// emptyC2
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw51(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasOver
		// emptyC1
		// emptyC2
		// within
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw96(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// emptyC2
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				continue;
			}

			int c1mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw97(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// emptyC2
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw98(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasOver
		// emptyC2
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw99(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasOver
		// emptyC2
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw100(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// emptyC2
		// fullC1
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				continue;
			}

			int c1mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw101(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// emptyC2
		// fullC1
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw102(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasOver
		// emptyC2
		// fullC1
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw103(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasOver
		// emptyC2
		// fullC1
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw114(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasC1
		// hasOver
		// emptyC1
		// emptyC2
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw115(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasC1
		// hasOver
		// emptyC1
		// emptyC2
		// within
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw144(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// emptyC1
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				continue;
			}

			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw145(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// emptyC1
		// within
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw146(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// hasOver
		// emptyC1
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw147(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// hasOver
		// emptyC1
		// within
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw152(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// emptyC1
		// fullC2
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				continue;
			}

			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw153(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// emptyC1
		// fullC2
		// within
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw154(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// hasOver
		// emptyC1
		// fullC2
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw155(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC2
		// hasOver
		// emptyC1
		// fullC2
		// within
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw178(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasC2
		// hasOver
		// emptyC1
		// emptyC2
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos++;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw179(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasC2
		// hasOver
		// emptyC1
		// emptyC2
		// within
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw192(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;
			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw193(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;
			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw194(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;
			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw195(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;
			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw196(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// fullC1
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw197(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// fullC1
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw198(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// fullC1
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw199(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// fullC1
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw200(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// fullC2
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw201(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// fullC2
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw202(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// fullC2
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw203(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// fullC2
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw204(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// fullC1
		// fullC2
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw205(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// fullC1
		// fullC2
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw206(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// fullC1
		// fullC2
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw207(int[] img, int w, int h, byte[] sym, int x, int y, int c1, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// fullC1
		// fullC2
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw208(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// emptyC1
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			pos++;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw209(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// emptyC1
		// within
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			pos++;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw210(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC1
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			pos++;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw211(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC1
		// within
		int c2userA = c2 >>> 24;
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			pos++;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c2A = (((c2userA * c2mask) << 8) + (c2userA * c2mask) + 32896) >>> 16;

			int invC2A = 255 - c2A;

			inR = (((c2R * c2A + inR * invC2A) << 8) + (c2R * c2A + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2A + inG * invC2A) << 8) + (c2G * c2A + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2A + inB * invC2A) << 8) + (c2B * c2A + inB * invC2A) + 32896) >>> 16;
			inA = (((c2A * 255 + inA * invC2A) << 8) + (c2A * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw216(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// emptyC1
		// fullC2
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			pos++;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw217(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// emptyC1
		// fullC2
		// within
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			pos++;
			int c2mask = sym[pos++] & 255;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw218(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC1
		// fullC2
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			pos++;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw219(int[] img, int w, int h, byte[] sym, int x, int y, int c2)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC1
		// fullC2
		// within
		int c2R = (c2 >>> 16) & 255;
		int c2G = (c2 >>> 8) & 255;
		int c2B = c2 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			pos++;
			int c2mask = sym[pos++] & 255;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC2A = 255 - c2mask;

			inR = (((c2R * c2mask + inR * invC2A) << 8) + (c2R * c2mask + inR * invC2A) + 32896) >>> 16;
			inG = (((c2G * c2mask + inG * invC2A) << 8) + (c2G * c2mask + inG * invC2A) + 32896) >>> 16;
			inB = (((c2B * c2mask + inB * invC2A) << 8) + (c2B * c2mask + inB * invC2A) + 32896) >>> 16;
			inA = (((c2mask * 255 + inA * invC2A) << 8) + (c2mask * 255 + inA * invC2A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw224(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// emptyC2
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			pos++;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw225(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// emptyC2
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			pos++;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw226(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC2
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw227(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC2
		// within
		int c1userA = c1 >>> 24;
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int c1A = (((c1userA * c1mask) << 8) + (c1userA * c1mask) + 32896) >>> 16;

			int invC1A = 255 - c1A;

			inR = (((c1R * c1A + inR * invC1A) << 8) + (c1R * c1A + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1A + inG * invC1A) << 8) + (c1G * c1A + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1A + inB * invC1A) << 8) + (c1B * c1A + inB * invC1A) + 32896) >>> 16;
			inA = (((c1A * 255 + inA * invC1A) << 8) + (c1A * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw228(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// emptyC2
		// fullC1
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			pos++;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw229(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// emptyC2
		// fullC1
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			pos++;

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw230(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC2
		// fullC1
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			int c1mask = sym[pos++] & 255;
			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw231(int[] img, int w, int h, byte[] sym, int x, int y, int c1)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC2
		// fullC1
		// within
		int c1R = (c1 >>> 16) & 255;
		int c1G = (c1 >>> 8) & 255;
		int c1B = c1 & 255;

		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			int c1mask = sym[pos++] & 255;
			pos++;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			int invC1A = 255 - c1mask;

			inR = (((c1R * c1mask + inR * invC1A) << 8) + (c1R * c1mask + inR * invC1A) + 32896) >>> 16;
			inG = (((c1G * c1mask + inG * invC1A) << 8) + (c1G * c1mask + inG * invC1A) + 32896) >>> 16;
			inB = (((c1B * c1mask + inB * invC1A) << 8) + (c1B * c1mask + inB * invC1A) + 32896) >>> 16;
			inA = (((c1mask * 255 + inA * invC1A) << 8) + (c1mask * 255 + inA * invC1A) + 32896) >>> 16;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw242(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC1
		// emptyC2
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			if (outX < 0 || outY < 0 || outX >= w || outY >= h) {
				pos += 2;
				if (sym[pos++] != 0)
					pos += 3;
				continue;
			}

			pos += 2;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}

	static void draw243(int[] img, int w, int h, byte[] sym, int x, int y)
	{
		// hasC1
		// hasC2
		// hasOver
		// emptyC1
		// emptyC2
		// within
		int pos = 5;
		int symLen = sym.length;
		while (pos < symLen) {
			int outX = x + sym[pos++];
			int outY = y + sym[pos++];

			pos += 2;

			int overA = sym[pos++] & 255;
			int overR, overG, overB;
			if (overA != 0) {
				overR = sym[pos++] & 255;
				overG = sym[pos++] & 255;
				overB = sym[pos++] & 255;
				if (overA == 255) {
					img[outY * w + outX] = 0xFF000000 | (overR << 16) | (overG << 8) | overB;
					continue;
				}
			} else {
				overR = overG = overB = 0;
			}

			int inRGB = img[outY * w + outX];
			int inA = inRGB >>> 24;
			int inR = (inRGB >>> 16) & 255;
			int inG = (inRGB >>> 8) & 255;
			int inB = inRGB & 255;

			if (overA != 0) {
				int invOverA = 255 - overA;

				inR = (((overR * overA + inR * invOverA) << 8) + (overR * overA + inR * invOverA) + 32896) >>> 16;
				inG = (((overG * overA + inG * invOverA) << 8) + (overG * overA + inG * invOverA) + 32896) >>> 16;
				inB = (((overB * overA + inB * invOverA) << 8) + (overB * overA + inB * invOverA) + 32896) >>> 16;
				inA = (((overA * 255 + inA * invOverA) << 8) + (overA * 255 + inA * invOverA) + 32896) >>> 16;
			}

			img[outY * w + outX] = (inA << 24) | (inR << 16) | (inG << 8) | inB;
		}
	}
}