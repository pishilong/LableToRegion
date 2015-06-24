clear, clc;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%directories??

% cd ..
% cd ..
% 
% root=cd;
% addpath(genpath([root '/SLEP']));
%                      % add the functions in the folder SLEP to the path
%                    
% % change to the original folder
% cd Examples/L1;


m=7;  
n=18;    % The data matrix is of size m x n

p=7;
q=12;

% m=1716;
% n=16168;
% p=1716;
% q=14452;


m=1716;
n=3716;
p=1716;
q=2000;

% ---------------------- Generate random data ----------------------

A=randn(m,n);       % the data matrix
y=randn(p,q);        % the Y matrix

rho=0.1;            % the regularization parameter
                    % it is a ratio between (0,1), if .rFlag=1

%----------------------- Set optional items ------------------------
opts=[];

% Starting point
opts.init=2;        % starting from a zero point

% termination criterion
opts.tFlag=5;       % run .maxIter iterations
opts.maxIter=1000;   % maximum number of iterations

% normalization
opts.nFlag=0;       % without normalization

% regularization
opts.rFlag=0;       % the input parameter 'rho' is a ratio in (0, 1)
%opts.rFlag=0; %!!!the program uses the input values for 'nameda' and 'row'
%opts.rsL2=0.01;     % the squared two norm term

%----------------------- Run the code LeastR -----------------------
fprintf('\n mFlag=0, lFlag=0 \n');
opts.mFlag=0;       % treating it as compositive function 
opts.lFlag=0;       % Nemirovski's line search
disp('opts:');
disp(opts);
tic;
disp('==========START============');
[x1]= LeastRBatch(A, y, rho, opts);
disp('final result x:')
disp(size(x1));
disp(x1);


%disp(ValueL1);
disp('==========END============');
% disp(ValueL1);

toc;
