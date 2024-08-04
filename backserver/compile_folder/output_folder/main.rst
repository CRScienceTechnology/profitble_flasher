                                      1 ;--------------------------------------------------------
                                      2 ; File Created by SDCC : free open source ANSI-C Compiler
                                      3 ; Version 4.0.0 #11528 (Linux)
                                      4 ;--------------------------------------------------------
                                      5 	.module main
                                      6 	.optsdcc -mmcs51 --model-small
                                      7 	
                                      8 ;--------------------------------------------------------
                                      9 ; Public variables in this module
                                     10 ;--------------------------------------------------------
                                     11 	.globl _main
                                     12 ;--------------------------------------------------------
                                     13 ; special function registers
                                     14 ;--------------------------------------------------------
                                     15 	.area RSEG    (ABS,DATA)
      000000                         16 	.org 0x0000
                                     17 ;--------------------------------------------------------
                                     18 ; special function bits
                                     19 ;--------------------------------------------------------
                                     20 	.area RSEG    (ABS,DATA)
      000000                         21 	.org 0x0000
                                     22 ;--------------------------------------------------------
                                     23 ; overlayable register banks
                                     24 ;--------------------------------------------------------
                                     25 	.area REG_BANK_0	(REL,OVR,DATA)
      000000                         26 	.ds 8
                                     27 ;--------------------------------------------------------
                                     28 ; internal ram data
                                     29 ;--------------------------------------------------------
                                     30 	.area DSEG    (DATA)
                                     31 ;--------------------------------------------------------
                                     32 ; overlayable items in internal ram 
                                     33 ;--------------------------------------------------------
                                     34 ;--------------------------------------------------------
                                     35 ; Stack segment in internal ram 
                                     36 ;--------------------------------------------------------
                                     37 	.area	SSEG
      000008                         38 __start__stack:
      000008                         39 	.ds	1
                                     40 
                                     41 ;--------------------------------------------------------
                                     42 ; indirectly addressable internal ram data
                                     43 ;--------------------------------------------------------
                                     44 	.area ISEG    (DATA)
                                     45 ;--------------------------------------------------------
                                     46 ; absolute internal ram data
                                     47 ;--------------------------------------------------------
                                     48 	.area IABS    (ABS,DATA)
                                     49 	.area IABS    (ABS,DATA)
                                     50 ;--------------------------------------------------------
                                     51 ; bit data
                                     52 ;--------------------------------------------------------
                                     53 	.area BSEG    (BIT)
                                     54 ;--------------------------------------------------------
                                     55 ; paged external ram data
                                     56 ;--------------------------------------------------------
                                     57 	.area PSEG    (PAG,XDATA)
                                     58 ;--------------------------------------------------------
                                     59 ; external ram data
                                     60 ;--------------------------------------------------------
                                     61 	.area XSEG    (XDATA)
                                     62 ;--------------------------------------------------------
                                     63 ; absolute external ram data
                                     64 ;--------------------------------------------------------
                                     65 	.area XABS    (ABS,XDATA)
                                     66 ;--------------------------------------------------------
                                     67 ; external initialized ram data
                                     68 ;--------------------------------------------------------
                                     69 	.area XISEG   (XDATA)
                                     70 	.area HOME    (CODE)
                                     71 	.area GSINIT0 (CODE)
                                     72 	.area GSINIT1 (CODE)
                                     73 	.area GSINIT2 (CODE)
                                     74 	.area GSINIT3 (CODE)
                                     75 	.area GSINIT4 (CODE)
                                     76 	.area GSINIT5 (CODE)
                                     77 	.area GSINIT  (CODE)
                                     78 	.area GSFINAL (CODE)
                                     79 	.area CSEG    (CODE)
                                     80 ;--------------------------------------------------------
                                     81 ; interrupt vector 
                                     82 ;--------------------------------------------------------
                                     83 	.area HOME    (CODE)
      000000                         84 __interrupt_vect:
      000000 02 00 06         [24]   85 	ljmp	__sdcc_gsinit_startup
                                     86 ;--------------------------------------------------------
                                     87 ; global & static initialisations
                                     88 ;--------------------------------------------------------
                                     89 	.area HOME    (CODE)
                                     90 	.area GSINIT  (CODE)
                                     91 	.area GSFINAL (CODE)
                                     92 	.area GSINIT  (CODE)
                                     93 	.globl __sdcc_gsinit_startup
                                     94 	.globl __sdcc_program_startup
                                     95 	.globl __start__stack
                                     96 	.globl __mcs51_genXINIT
                                     97 	.globl __mcs51_genXRAMCLEAR
                                     98 	.globl __mcs51_genRAMCLEAR
                                     99 	.area GSFINAL (CODE)
      00005F 02 00 03         [24]  100 	ljmp	__sdcc_program_startup
                                    101 ;--------------------------------------------------------
                                    102 ; Home
                                    103 ;--------------------------------------------------------
                                    104 	.area HOME    (CODE)
                                    105 	.area HOME    (CODE)
      000003                        106 __sdcc_program_startup:
      000003 02 00 62         [24]  107 	ljmp	_main
                                    108 ;	return from main will return to caller
                                    109 ;--------------------------------------------------------
                                    110 ; code
                                    111 ;--------------------------------------------------------
                                    112 	.area CSEG    (CODE)
                                    113 ;------------------------------------------------------------
                                    114 ;Allocation info for local variables in function 'main'
                                    115 ;------------------------------------------------------------
                                    116 ;	/root/REMOTE_COMPILER/compile_folder/source_code_folder/main.c:1: int main()
                                    117 ;	-----------------------------------------
                                    118 ;	 function main
                                    119 ;	-----------------------------------------
      000062                        120 _main:
                           000007   121 	ar7 = 0x07
                           000006   122 	ar6 = 0x06
                           000005   123 	ar5 = 0x05
                           000004   124 	ar4 = 0x04
                           000003   125 	ar3 = 0x03
                           000002   126 	ar2 = 0x02
                           000001   127 	ar1 = 0x01
                           000000   128 	ar0 = 0x00
                                    129 ;	/root/REMOTE_COMPILER/compile_folder/source_code_folder/main.c:3: return 0;
      000062 90 00 00         [24]  130 	mov	dptr,#0x0000
                                    131 ;	/root/REMOTE_COMPILER/compile_folder/source_code_folder/main.c:4: }
      000065 22               [24]  132 	ret
                                    133 	.area CSEG    (CODE)
                                    134 	.area CONST   (CODE)
                                    135 	.area XINIT   (CODE)
                                    136 	.area CABS    (ABS,CODE)
