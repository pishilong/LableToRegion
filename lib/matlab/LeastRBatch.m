function [x]=LeastRBatcher(A, y, z, opts)

% Verify the number of input parameters
if (nargin <3)
    error('\n Inputs: A, y and z should be specified!\n');
elseif (nargin==3)
    opts=[];
end

% Get the size of the matrix A
[m,n] = size(A);
[p,q] = size(y);

disp(size(A));
disp(size(y));

disp('opts in LeastRBatch:');
disp(opts);

disp('rho in LeastRBatch:');
disp(z);

% disp('A');
% disp(A);
% 
% disp('y');
% disp(y);

% Verify the length of y
if (p ~=m)
    error('\n Check A y for their degrees!\n');
end

% Verify the value of z
if (z<0)
    error('\n z should be nonnegative!\n');
end

totalRegionCount = q;

startRegion = 1; %MatLab 's array index starts from 1£¡
endRegion = 10;


%X = zeros(n, q);
X = [];

for i=startRegion:1:endRegion
    %get A(i) from A' £¨remove ith column data from A'£©
    Ai = A;
    Ai(:,[i]) = [];
    
	%get y(i) from Y' (get the ith column of Y')
    yi = y(:,i);
    
	%calc x(i) = leastR(A(i), y(i), rho, opts)
    disp(['==========Start calculating leastR ' num2str(i) '==========']);
    tic;
    disp('size(Ai)')
    disp(size(Ai));
    disp('size(yi)');
    disp(size(yi));
    [xi, funVal1, ValueL1]= LeastR(Ai, yi, z, opts);
    %disp(xi);
    toc;
    
	%append x(i) to x'  (add a column)
    X = [X, xi];
    disp('size(X):');
    disp(size(X));
end

x = X;
disp('==========Done Calling LeastRWrapper!============');


